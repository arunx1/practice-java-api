package io.github.arunx1.practice.java.api.service;

import io.github.arunx1.practice.java.api.dto.UserDto;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public UserService(JdbcTemplate jdbcTemplate, StringRedisTemplate redisTemplate, ObjectMapper objectMapper){
        this.jdbcTemplate = jdbcTemplate;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    private String cacheKey(Integer id) {
        return "user:" + id;
    }

    private final RowMapper<UserDto> userRowMapper = new RowMapper<UserDto>() {
        @Override
        public UserDto mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new UserDto(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    null
            );
        }
    };

    public List<UserDto> listUsers() {
        String sql = "SELECT id, name, email FROM users ORDER BY id ASC";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    public Optional<UserDto> getUserFromCache(Integer id) {
        String key = cacheKey(id);
        String userJson = redisTemplate.opsForValue().get(key);
        if (userJson == null)
            return Optional.empty();
        try {
            UserDto dto = objectMapper.readValue(userJson, UserDto.class);
            return Optional.of(new UserDto(dto.id(), dto.name(), dto.email(), "cache"));
        } catch (JacksonException e) {
            redisTemplate.delete(key);
            return Optional.empty();
        }
    }

    public Optional<UserDto> getUserFromDb(Integer id) {
        String sql = "SELECT id, name, email FROM users WHERE id = ?";
        try {
            UserDto dto = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            if (dto == null)
                return Optional.empty();
            return Optional.of(new UserDto(dto.id(), dto.name(), dto.email(), "db"));
        } catch (EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }

    public Optional<UserDto> getUser(Integer id) {
        Optional<UserDto> cachedUser = getUserFromCache(id);
        if (cachedUser.isPresent())
            return cachedUser;
        Optional<UserDto> dbUser = getUserFromDb(id);
        dbUser.ifPresent(userDto -> {
            UserDto toCache = new UserDto(userDto.id(), userDto.name(), userDto.email(), null);
            try {
                redisTemplate.opsForValue().set(cacheKey(id), objectMapper.writeValueAsString(toCache));
            } catch (JacksonException e) {
                System.out.println("Failed to cache user: " + e.getMessage());
            }
        });
        return dbUser;
    }

    public boolean isPostgresUp() {
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRedisUp() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            return "PONG".equalsIgnoreCase(pong);
        } catch (Exception e) {
            return false;
        }
    }
}

package io.github.arunx1.practice.java.api.controller;

import io.github.arunx1.practice.java.api.dto.UserDto;
import io.github.arunx1.practice.java.api.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        boolean pg = userService.isPostgresUp();
        boolean redis = userService.isRedisUp();
        return new HashMap<>(){{
            put("status", (pg && redis) ? "ok" : "degraded");
            put("postgres", pg ? "up" : "down");
            put("redis", redis ? "up" : "down");
            put("service", "java-api");
        }};
    }

    @GetMapping("/users")
    public List<UserDto> listUsers() {
        return userService.listUsers();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUser(@PathVariable Integer id) {
        return userService.getUser(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(Map.of("message","User not found"))
                );
    }
}

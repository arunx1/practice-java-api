package io.github.arunx1.practice.java.api.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
public class UserController {
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> resp = new HashMap<>();
        resp.put("status", "ok");
        resp.put("service", "java-api");
        return resp;
    }

    @GetMapping("/users")
    public List<Map<String, Object>> listUsers() {
        // For now just a static stub.
        // Later we'll fetch from Postgres and/or Python API + Redis.
        Map<String, Object> user = new HashMap<>();
        user.put("id", 1);
        user.put("name", "stub-user");
        user.put("source", "java-api-stub");
        return List.of(user);
    }
}

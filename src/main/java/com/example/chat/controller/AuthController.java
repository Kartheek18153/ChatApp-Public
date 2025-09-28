package com.example.chat.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Simple in-memory user storage (username → password)
    private Map<String, String> users = new ConcurrentHashMap<>();

    // ✅ Registration Endpoint
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Username and password are required!");
        }

        if (users.containsKey(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                                 .body("User already exists!");
        }

        users.put(username, password);
        return ResponseEntity.ok("Registration successful!");
    }

    // ✅ Login Endpoint
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                 .body("Username and password are required!");
        }

        if (users.containsKey(username) && users.get(username).equals(password)) {
            return ResponseEntity.ok("Login successful!");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body("Invalid username or password!");
        }
    }
}

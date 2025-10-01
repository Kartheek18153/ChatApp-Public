package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository repo;

    public AuthController(UserRepository repo) {
        this.repo = repo;
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (repo.findByUsername(user.getUsername()) != null) {
            return "User already exists!";
        }
        repo.save(user);
        return "Registration successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        User u = repo.findByUsername(user.getUsername());
        if (u != null && u.getPassword().equals(user.getPassword())) {
            return "Login successful";
        }
        return "Invalid credentials";
    }
}

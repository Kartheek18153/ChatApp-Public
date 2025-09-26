package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, Object> res = new HashMap<>();

        if (userRepository.existsByUsername(username)) {
            res.put("success", false);
            res.put("message", "Username already exists!");
            return res;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);

        // First user becomes ADMIN automatically
        if (userRepository.count() == 0) {
            user.setRole("ADMIN");
        }

        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Registered successfully!");
        res.put("role", user.getRole());
        return res;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        Map<String, Object> res = new HashMap<>();
        User user = userRepository.findByUsername(username);

        if (user == null || !user.getPassword().equals(password)) {
            res.put("success", false);
            res.put("message", "Invalid credentials!");
        } else {
            res.put("success", true);
            res.put("message", "Login successful!");
            res.put("role", user.getRole());
        }
        return res;
    }
}

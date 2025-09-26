package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody Map<String,String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        Map<String,Object> response = new HashMap<>();
        if(userRepository.findByUsername(username).isPresent()) {
            response.put("success", false);
            response.put("message", "Username already exists!");
            return response;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Registration successful!");
        return response;
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Map<String,String> payload) {
        String username = payload.get("username");
        String password = payload.get("password");

        Map<String,Object> response = new HashMap<>();
        User user = userRepository.findByUsername(username).orElse(null);

        if(user == null || !user.getPassword().equals(password)) {
            response.put("success", false);
            response.put("message", "Invalid credentials!");
        } else {
            response.put("success", true);
            response.put("message", "Login successful!");
        }
        return response;
    }
}

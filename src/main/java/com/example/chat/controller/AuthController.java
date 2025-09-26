package com.example.chat.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;

import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public Map<String,Object> register(@RequestBody Map<String,String> userMap) {
        String username = userMap.get("username");
        String password = userMap.get("password");
        Map<String,Object> response = new HashMap<>();

        if(userRepository.findByUsername(username)!=null) {
            response.put("success", false);
            response.put("message", "Username already exists");
        } else {
            User user = new User();
            user.setUsername(username);
            user.setPassword(password);
            userRepository.save(user);
            response.put("success", true);
            response.put("message", "Registration successful");
        }
        return response;
    }

    @PostMapping("/login")
    public Map<String,Object> login(@RequestBody Map<String,String> userMap) {
        String username = userMap.get("username");
        String password = userMap.get("password");
        Map<String,Object> response = new HashMap<>();
        User user = userRepository.findByUsername(username);

        if(user != null && user.getPassword().equals(password)) {
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("role", user.getRole());
        } else {
            response.put("success", false);
            response.put("message", "Invalid credentials");
        }
        return response;
    }
}

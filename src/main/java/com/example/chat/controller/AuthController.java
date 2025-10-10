
package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return "Username and password are required";
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return "Username already exists!";
        }
        userRepository.save(user);
        return "Registration successful!";
    }

    @PostMapping("/login")
    public String login(@RequestBody User user) {
        if (user.getUsername() == null || user.getPassword() == null) {
            return "Username and password are required";
        }
        return userRepository.findByUsername(user.getUsername())
                .map(u -> u.getPassword().equals(user.getPassword())
                        ? "Login successful!"
                        : "Invalid password!")
                .orElse("Invalid username!");
    }
} 

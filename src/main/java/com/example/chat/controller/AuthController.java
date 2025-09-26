package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // ------------------ REGISTER ------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if(userRepository.existsByUsername(user.getUsername())) {
            return ResponseEntity.ok().body("{\"success\":false,\"message\":\"Username already exists\"}");
        }

        // If registering first user, make them admin (optional)
        if(userRepository.count() == 0) {
            user.setAdmin(true);
        }

        userRepository.save(user);
        return ResponseEntity.ok().body("{\"success\":true,\"message\":\"User registered successfully\"}");
    }

    // ------------------ LOGIN ------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if(optionalUser.isEmpty() || !optionalUser.get().getPassword().equals(user.getPassword())) {
            return ResponseEntity.ok().body("{\"success\":false,\"message\":\"Invalid username or password\"}");
        }

        User loggedInUser = optionalUser.get();
        // Return username and isAdmin flag
        String response = String.format("{\"success\":true,\"message\":\"Login successful\",\"username\":\"%s\",\"isAdmin\":%s}",
                loggedInUser.getUsername(), loggedInUser.isAdmin());
        return ResponseEntity.ok().body(response);
    }
}

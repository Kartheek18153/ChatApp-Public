package com.example.clario.controller;

import com.example.clario.model.User;
import com.example.clario.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        userRepository.save(user);
        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }
        Optional<User> dbUser = userRepository.findByUsername(user.getUsername());
        if(dbUser.isEmpty()) return ResponseEntity.status(401).body("Invalid username");
        if(!dbUser.get().getPassword().equals(user.getPassword())) return ResponseEntity.status(401).body("Invalid password");
        return ResponseEntity.ok("Login successful");
    }
}

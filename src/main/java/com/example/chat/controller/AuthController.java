package com.example.chat.controller;

import com.example.chat.handler.ChatWebSocketHandler;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public AuthController(UserRepository userRepository, ChatWebSocketHandler chatWebSocketHandler) {
        this.userRepository = userRepository;
        this.chatWebSocketHandler = chatWebSocketHandler;
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

        // Broadcast SYSTEM message
        chatWebSocketHandler.broadcastSystem(user.getUsername() + " has joined the chat");

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {
        if(user.getUsername() == null || user.getPassword() == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if(optionalUser.isEmpty()) return ResponseEntity.status(401).body("Invalid username");

        if(!optionalUser.get().getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body("Invalid password");
        }

        // Broadcast SYSTEM message
        chatWebSocketHandler.broadcastSystem(user.getUsername() + " has joined the chat");

        return ResponseEntity.ok("Login successful");
    }
}

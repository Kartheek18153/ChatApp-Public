package com.example.chat.controller;

import com.example.chat.handler.ChatWebSocketHandler;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
    public ResponseEntity<String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        if (userRepository.findByUsername(username).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        userRepository.save(user);

        // Broadcast system message to all clients
        chatWebSocketHandler.broadcastSystemMessage(username + " has joined the chat");

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        if (username == null || password == null) {
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        return userRepository.findByUsername(username)
                .map(user -> {
                    if (user.getPassword().equals(password)) {
                        // Broadcast system message when user logs in
                        chatWebSocketHandler.broadcastSystemMessage(username + " is online");
                        return ResponseEntity.ok("Login successful");
                    } else {
                        return ResponseEntity.status(401).body("Invalid password");
                    }
                })
                .orElse(ResponseEntity.status(401).body("Invalid username"));
    }
}

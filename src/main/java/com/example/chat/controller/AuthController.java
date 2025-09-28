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
        if(user.getUsername() == null || user.getPassword() == null){
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        Optional<User> exists = userRepository.findByUsername(user.getUsername());
        if(exists.isPresent()){
            return ResponseEntity.badRequest().body("Username already exists");
        }

        userRepository.save(user);

        // Notify all users
        chatWebSocketHandler.broadcastSystemMessage(user.getUsername() + " joined the chat");

        return ResponseEntity.ok("Registration successful");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user){
        if(user.getUsername() == null || user.getPassword() == null){
            return ResponseEntity.badRequest().body("Username and password are required");
        }

        Optional<User> exists = userRepository.findByUsername(user.getUsername());
        if(!exists.isPresent()){
            return ResponseEntity.status(401).body("Invalid username");
        }

        if(!exists.get().getPassword().equals(user.getPassword())){
            return ResponseEntity.status(401).body("Invalid password");
        }

        // Notify all users
        chatWebSocketHandler.broadcastSystemMessage(user.getUsername() + " joined the chat");

        return ResponseEntity.ok("Login successful");
    }
}

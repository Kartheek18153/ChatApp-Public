package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.model.User;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    // Registration
    @PostMapping("/register")
    public Response register(@RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return new Response(false, "Username already exists!");
        }
        user.setRole("USER"); // default role
        userRepository.save(user);
        return new Response(true, "Registration successful!");
    }

    // Login
    @PostMapping("/login")
    public Response login(@RequestBody User user) {
        Optional<User> existing = userRepository.findByUsername(user.getUsername());
        if (existing.isPresent() && existing.get().getPassword().equals(user.getPassword())) {
            return new Response(true, "Login successful!", existing.get().getRole());
        }
        return new Response(false, "Invalid username or password!");
    }

    // Get all messages
    @GetMapping("/messages")
    public List<Message> getMessages() {
        return messageRepository.findAll();
    }

    // Admin: delete all messages
    @DeleteMapping("/admin/deleteAllMessages")
    public Response deleteAllMessages(@RequestParam String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isPresent() && "ADMIN".equals(user.get().getRole())) {
            messageRepository.deleteAll();
            return new Response(true, "All messages deleted by admin!");
        }
        return new Response(false, "Unauthorized: Only admin can delete messages.");
    }

    // Helper response class
    static class Response {
        public boolean success;
        public String message;
        public String role;

        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public Response(boolean success, String message, String role) {
            this.success = success;
            this.message = message;
            this.role = role;
        }
    }
}

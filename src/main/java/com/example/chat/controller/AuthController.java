package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // ----------------- REGISTER -----------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.ok().body(new Response(false, "Username already exists!"));
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // default role
        userRepository.save(user);
        return ResponseEntity.ok().body(new Response(true, "Registration successful!"));
    }

    // ----------------- LOGIN -----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        Optional<User> optionalUser = userRepository.findByUsername(user.getUsername());
        if(optionalUser.isEmpty()) {
            return ResponseEntity.ok().body(new Response(false, "User not found!"));
        }

        User existingUser = optionalUser.get();
        if(!passwordEncoder.matches(user.getPassword(), existingUser.getPassword())) {
            return ResponseEntity.ok().body(new Response(false, "Incorrect password!"));
        }

        return ResponseEntity.ok().body(new LoginResponse(true, "Login successful!", existingUser.getRole()));
    }

    // ----------------- DELETE ALL MESSAGES (ADMIN ONLY) -----------------
    @DeleteMapping("/deleteAllMessages")
    public ResponseEntity<?> deleteAllMessages(@RequestParam String username) {
        Optional<User> optionalUser = userRepository.findByUsername(username);
        if(optionalUser.isEmpty()) {
            return ResponseEntity.ok().body(new Response(false, "User not found!"));
        }

        User user = optionalUser.get();
        if(!user.getRole().equals("ADMIN")) {
            return ResponseEntity.ok().body(new Response(false, "Only admin can delete messages!"));
        }

        messageRepository.deleteAll();
        return ResponseEntity.ok().body(new Response(true, "All messages deleted!"));
    }

    // ----------------- RESPONSE CLASSES -----------------
    static class Response {
        public boolean success;
        public String message;
        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    static class LoginResponse extends Response {
        public String role;
        public LoginResponse(boolean success, String message, String role) {
            super(success, message);
            this.role = role;
        }
    }
}

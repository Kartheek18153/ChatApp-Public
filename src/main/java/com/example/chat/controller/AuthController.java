package com.example.chat.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final ConcurrentHashMap<String, String> users = new ConcurrentHashMap<>();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO user) {
        if(users.containsKey(user.username)){
            return ResponseEntity.ok(new MessageResponse(false, "Username already exists!"));
        }
        users.put(user.username, user.password);
        return ResponseEntity.ok(new MessageResponse(true, "Registration successful!"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserDTO user) {
        if(users.containsKey(user.username) && users.get(user.username).equals(user.password)){
            return ResponseEntity.ok(new MessageResponse(true, "Login successful!"));
        }
        return ResponseEntity.ok(new MessageResponse(false, "Invalid username or password!"));
    }

    static class UserDTO {
        public String username;
        public String password;
    }

    static class MessageResponse {
        public boolean success;
        public String message;
        public MessageResponse(boolean success, String message){
            this.success = success;
            this.message = message;
        }
    }
}

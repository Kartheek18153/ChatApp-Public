package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String,Object>> register(@RequestBody Map<String,String> data) {
        String username = data.get("username");
        String password = data.get("password");

        Map<String,Object> resp = new HashMap<>();
        if(userRepository.findByUsername(username) != null){
            resp.put("success", false);
            resp.put("message", "Username already exists");
            return ResponseEntity.ok(resp);
        }

        User user = new User(username,password);
        userRepository.save(user);

        resp.put("success", true);
        resp.put("message", "Registered successfully!");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String,Object>> login(@RequestBody Map<String,String> data){
        String username = data.get("username");
        String password = data.get("password");

        Map<String,Object> resp = new HashMap<>();
        User user = userRepository.findByUsername(username);
        if(user == null || !user.getPassword().equals(password)){
            resp.put("success", false);
            resp.put("message", "Invalid credentials");
            return ResponseEntity.ok(resp);
        }

        resp.put("success", true);
        resp.put("message", "Logged in successfully!");
        return ResponseEntity.ok(resp);
    }
}

package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    // Registration
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> req){
        Map<String,Object> res = new HashMap<>();
        String username = req.get("username");
        String password = req.get("password");

        if(username == null || password == null){
            res.put("success", false);
            res.put("message", "Username and password required");
            return res;
        }

        if(userRepository.findByUsername(username).isPresent()){
            res.put("success", false);
            res.put("message", "Username already exists");
            return res;
        }

        // First user registered becomes admin
        boolean isAdmin = userRepository.count() == 0;

        User user = new User(username, password, isAdmin);
        userRepository.save(user);

        res.put("success", true);
        res.put("message", "Registration successful" + (isAdmin ? " (You are admin)" : ""));
        return res;
    }

    // Login
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String,String> req){
        Map<String,Object> res = new HashMap<>();
        String username = req.get("username");
        String password = req.get("password");

        if(username == null || password == null){
            res.put("success", false);
            res.put("message", "Username and password required");
            return res;
        }

        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null || !user.getPassword().equals(password)){
            res.put("success", false);
            res.put("message", "Invalid username or password");
            return res;
        }

        res.put("success", true);
        res.put("message", "Login successful");
        res.put("isAdmin", user.isAdmin());
        return res;
    }
}

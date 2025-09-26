package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
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
    public Map<String, Object> register(@RequestBody Map<String, String> body){
        Map<String,Object> resp = new HashMap<>();
        String username = body.get("username");
        String password = body.get("password");

        if(userRepository.findByUsername(username) != null){
            resp.put("success", false);
            resp.put("message", "Username already exists!");
            return resp;
        }

        userRepository.save(new User(username,password));
        resp.put("success", true);
        resp.put("message", "Registered successfully!");
        return resp;
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body){
        Map<String,Object> resp = new HashMap<>();
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username);
        if(user != null && user.getPassword().equals(password)){
            resp.put("success", true);
            resp.put("message", "Login successful!");
        } else {
            resp.put("success", false);
            resp.put("message", "Invalid username or password!");
        }
        return resp;
    }
}

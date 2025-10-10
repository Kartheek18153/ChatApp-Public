package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    // Get user profile by username
    @GetMapping("/{username}")
    public User getProfile(@PathVariable String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.orElse(null); // return null if not found
    }

    // Update profile (only username for simplicity, password optional)
    @PutMapping("/{username}")
    public String updateProfile(@PathVariable String username, @RequestBody User updatedUser) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return "User not found!";

        User user = userOpt.get();
        if (updatedUser.getUsername() != null) user.setUsername(updatedUser.getUsername());
        if (updatedUser.getPassword() != null) user.setPassword(updatedUser.getPassword());
        userRepository.save(user);
        return "Profile updated successfully!";
    }
}

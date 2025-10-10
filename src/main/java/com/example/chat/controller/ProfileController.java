package com.example.chat.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.chat.model.User;
import com.example.chat.repository.UserRepository;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/profile")
@CrossOrigin(origins = "*")
public class ProfileController {

    @Autowired
    private UserRepository userRepo;

    // Get a user's profile
    @GetMapping("/{username}")
    public User getProfile(@PathVariable String username) {
        return userRepo.findByUsername(username);
    }

    // Update user profile
    @PostMapping("/update")
    public String updateProfile(@RequestBody User updatedUser) {
        User user = userRepo.findByUsername(updatedUser.getUsername());
        if (user == null) return "User not found.";

        if (updatedUser.getDisplayName() != null)
            user.setDisplayName(updatedUser.getDisplayName());
        if (updatedUser.getBio() != null)
            user.setBio(updatedUser.getBio());
        if (updatedUser.getAvatarUrl() != null)
            user.setAvatarUrl(updatedUser.getAvatarUrl());
        if (updatedUser.getStatus() != null)
            user.setStatus(updatedUser.getStatus());

        user.setLastSeen(LocalDateTime.now());
        userRepo.save(user);
        return "Profile updated successfully.";
    }
}

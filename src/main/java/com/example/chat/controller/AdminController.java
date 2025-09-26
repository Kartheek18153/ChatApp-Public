package com.example.chat.controller;

import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    // Delete all messages (Admin only)
    @DeleteMapping("/deleteAllMessages")
    public String deleteAllMessages(@RequestParam String username) {
        Optional<com.example.chat.model.User> userOpt = userRepository.findByUsername(username);
        if(userOpt.isEmpty() || !userOpt.get().isAdmin()) {
            return "Access denied: Only admins can delete messages!";
        }

        messageRepository.deleteAll();
        return "All messages deleted by admin!";
    }
}

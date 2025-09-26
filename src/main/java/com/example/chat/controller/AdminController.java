package com.example.chat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.chat.repository.MessageRepository;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MessageRepository messageRepository;

    @DeleteMapping("/deleteAllMessages")
    public String deleteAllMessages(@RequestParam String username) {
        if(!"admin".equalsIgnoreCase(username)) {
            return "You are not authorized to perform this action!";
        }
        messageRepository.deleteAll();
        return "All messages deleted!";
    }
}

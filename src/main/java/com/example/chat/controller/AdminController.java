package com.example.chat.controller;

import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MessageRepository messageRepository;

    // Delete all messages
    @DeleteMapping("/deleteAllMessages")
    public String deleteAllMessages() {
        messageRepository.deleteAll();
        return "All messages deleted!";
    }
}


package com.example.chat.controller;

import com.example.chat.repository.MessageRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {
    private final MessageRepository repo;

    public MessageController(MessageRepository repo) {
        this.repo = repo;
    }

    @DeleteMapping("/{id}")
    public void deleteMessage(@PathVariable Long id) {
        repo.deleteById(id);
    }
} 

package com.example.chat.controller;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public MessageController(MessageRepository messageRepository, SimpMessagingTemplate messagingTemplate) {
        this.messageRepository = messageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<Message> sendMessage(@RequestBody Message msg) {
        msg.setTimestamp(Instant.now());
        Message saved = messageRepository.save(msg);

        Map<String,Object> payload = new HashMap<>();
        payload.put("type","message");
        payload.put("id", saved.getId());
        payload.put("username", saved.getUsername());
        payload.put("content", saved.getContent());
        payload.put("timestamp", saved.getTimestamp());
        messagingTemplate.convertAndSend("/topic/messages", payload);

        return ResponseEntity.ok(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        Optional<Message> m = messageRepository.findById(id);
        if(m.isPresent()) {
            messageRepository.deleteById(id);
            Map<String,Object> payload = new HashMap<>();
            payload.put("type","delete");
            payload.put("id", id);
            messagingTemplate.convertAndSend("/topic/messages", payload);
            return ResponseEntity.ok("Deleted");
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Not found");
    }
}

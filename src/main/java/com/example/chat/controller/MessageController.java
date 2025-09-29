package com.example.chat.controller;

import com.example.chat.repository.MessageRepository;
import com.example.chat.handler.ChatWebSocketHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    private final MessageRepository messageRepository;
    private final ChatWebSocketHandler chatWebSocketHandler;

    public MessageController(MessageRepository messageRepository,
                             ChatWebSocketHandler chatWebSocketHandler) {
        this.messageRepository = messageRepository;
        this.chatWebSocketHandler = chatWebSocketHandler;
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id) {
        if (messageRepository.existsById(id)) {
            messageRepository.deleteById(id);

            // Broadcast delete event to all clients
            chatWebSocketHandler.broadcastDelete(id);

            return ResponseEntity.ok("Message deleted");
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}

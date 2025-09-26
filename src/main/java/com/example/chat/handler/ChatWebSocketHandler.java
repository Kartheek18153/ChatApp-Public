package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Optionally, you can send previous messages to the client when they connect
        for (Message msg : messageRepository.findAll()) {
            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        // Parse incoming JSON
        Message incomingMessage = objectMapper.readValue(textMessage.getPayload(), Message.class);

        // Set the timestamp correctly
        incomingMessage.setTimestamp(LocalDateTime.ofInstant(
                Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault()
        ));

        // Save to database
        messageRepository.save(incomingMessage);

        // Broadcast message to all sessions (simplified example)
        for (WebSocketSession s : session.getOpenSessions()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(objectMapper.writeValueAsString(incomingMessage)));
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Transport error: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }
}

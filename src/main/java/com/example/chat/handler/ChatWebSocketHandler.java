package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.BadWordRepository;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class ChatWebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    private final BadWordRepository badWordRepository;
    private final MessageRepository messageRepository;

    public ChatWebSocketHandler(BadWordRepository badWordRepository, MessageRepository messageRepository) {
        this.badWordRepository = badWordRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Send previous messages to new user
        for(Message msg : messageRepository.findTop100ByOrderByTimestampAsc()) {
            session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
        }
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        Map<String, Object> msg = mapper.readValue(message.getPayload().toString(), Map.class);
        String username = (String) msg.get("username");
        String content = ((String) msg.get("content")).toLowerCase();

        boolean containsBadWord = badWordRepository.findAll().stream()
                .anyMatch(b -> content.contains(b.getWord().toLowerCase()));

        if(containsBadWord) {
            // Send alert to sender only
            Map<String, Object> alert = new HashMap<>();
            alert.put("username","SYSTEM");
            alert.put("content","Message contains restricted word!");
            alert.put("timestamp",System.currentTimeMillis());
            session.sendMessage(new TextMessage(mapper.writeValueAsString(alert)));
            return;
        }

        Message msgEntity = new Message(username, (String) msg.get("content"), System.currentTimeMillis());
        messageRepository.save(msgEntity);

        // Broadcast to all users
        for(WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage(mapper.writeValueAsString(msgEntity)));
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {}

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() { return false; }
}

package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.BadWordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final BadWordRepository badWordRepository;
    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(MessageRepository messageRepository, BadWordRepository badWordRepository) {
        this.messageRepository = messageRepository;
        this.badWordRepository = badWordRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Send previous messages to new user
        for (Message msg : messageRepository.findAll()) {
            Map<String, Object> data = new HashMap<>();
            data.put("username", msg.getUsername());
            data.put("content", msg.getContent());
            data.put("timestamp", msg.getTimestamp());
            session.sendMessage(new TextMessage(mapper.writeValueAsString(data)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> map = mapper.readValue(message.getPayload(), Map.class);
        String username = map.get("username");
        String content = map.get("content");

        // Bad word detection
        String lowerContent = content.toLowerCase();
        boolean bad = badWordRepository.findAll().stream().anyMatch(b -> lowerContent.contains(b.getWord()));

        if (bad) {
            Map<String, Object> sysMsg = new HashMap<>();
            sysMsg.put("username", "SYSTEM");
            sysMsg.put("content", "Bad word detected from " + username + "!");
            sysMsg.put("timestamp", new Date());
            broadcast(sysMsg);
        }

        // Save and broadcast normal message
        Message newMsg = new Message(username, content);
        messageRepository.save(newMsg);
        Map<String, Object> data = new HashMap<>();
        data.put("username", username);
        data.put("content", content);
        data.put("timestamp", newMsg.getTimestamp());
        broadcast(data);
    }

    private void broadcast(Map<String, Object> data) throws Exception {
        String json = mapper.writeValueAsString(data);
        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage(json));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}

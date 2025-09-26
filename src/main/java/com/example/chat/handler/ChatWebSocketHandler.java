package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Send last 50 messages to the new user
        List<Message> lastMessages = messageRepository.findTop50ByOrderByTimestampAsc();
        for (Message msg : lastMessages) {
            session.sendMessage(new TextMessage(toJson(msg)));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        String payload = textMessage.getPayload();

        // Parse incoming JSON
        Map<String, Object> map = new com.fasterxml.jackson.databind.ObjectMapper().readValue(payload, Map.class);
        String username = (String) map.get("username");
        String content = (String) map.get("content");

        // Create message entity
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now().toEpochMilli()); // store timestamp as long

        // Save message in DB
        messageRepository.save(msg);

        // Broadcast message to all sessions
        String json = toJson(msg);
        for (WebSocketSession ws : sessions) {
            if (ws.isOpen()) {
                ws.sendMessage(new TextMessage(json));
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) {
        sessions.remove(session);
    }

    private String toJson(Message msg) {
        return String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":%d}",
                msg.getUsername(), msg.getContent(), msg.getTimestamp());
    }
}

package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MessageRepository messageRepository;

    // List of restricted words (can be expanded or moved to DB)
    private static final List<String> RESTRICTED_WORDS = Arrays.asList(
        "badword1", "badword2", "curse1"
    );

    // Admin sessions (for alerts)
    private final List<WebSocketSession> adminSessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Identify admin (for demo, username "admin" is admin)
        String username = (String) session.getAttributes().get("username");
        if ("admin".equalsIgnoreCase(username)) {
            adminSessions.add(session);
        }

        // Send previous messages to this session
        List<Message> history = messageRepository.findAllByOrderByTimestampAsc();
        for (Message m : history) {
            Map<String,Object> map = new HashMap<>();
            map.put("username", m.getUsername());
            map.put("content", m.getContent());
            map.put("timestamp", m.getTimestamp());
            session.sendMessage(new TextMessage(mapper.writeValueAsString(map)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        adminSessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String,Object> msg = mapper.readValue(message.getPayload(), Map.class);
        String username = (String) msg.get("username");
        String content = (String) msg.get("content");

        // Check for restricted words
        boolean containsRestricted = RESTRICTED_WORDS.stream()
                .anyMatch(word -> content.toLowerCase().contains(word));

        if (containsRestricted) {
            // Alert admin
            sendAdminAlert(username, content);
            // Optionally: do NOT broadcast to users
            return;
        }

        msg.put("timestamp", System.currentTimeMillis());

        // Save message to DB
        Message dbMsg = new Message();
        dbMsg.setUsername(username);
        dbMsg.setContent(content);
        dbMsg.setTimestamp((Long) msg.get("timestamp"));
        messageRepository.save(dbMsg);

        // Broadcast to all sessions
        String broadcast = mapper.writeValueAsString(msg);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        }
    }

    // Sends alert to all admin sessions
    private void sendAdminAlert(String user, String content) throws Exception {
        Map<String,Object> alert = new HashMap<>();
        alert.put("alert", true);
        alert.put("user", user);
        alert.put("content", content);
        alert.put("timestamp", System.currentTimeMillis());

        String alertMsg = mapper.writeValueAsString(alert);
        System.out.println("⚠️ Restricted word detected from user: " + user + " | Message: " + content);

        for (WebSocketSession admin : adminSessions) {
            if (admin.isOpen()) admin.sendMessage(new TextMessage(alertMsg));
        }
    }
}

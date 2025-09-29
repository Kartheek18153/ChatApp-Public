package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Send all previous messages to newly connected user
        messageRepository.findAll().forEach(msg -> {
            try {
                session.sendMessage(new TextMessage(
                        String.format("{\"type\":\"message\",\"id\":%d,\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                                msg.getId(), msg.getUsername(), msg.getContent(), msg.getTimestamp())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Parse simple JSON (basic parsing)
        String username = payload.split("\"username\":\"")[1].split("\"")[0];
        String content = payload.split("\"content\":\"")[1].split("\"")[0];

        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        messageRepository.save(msg);

        String broadcast = String.format("{\"type\":\"message\",\"id\":%d,\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                msg.getId(), msg.getUsername(), msg.getContent(), msg.getTimestamp());

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        }
    }

    // 🔹 New method for delete broadcast
    public void broadcastDelete(Long messageId) {
        String deleteEvent = String.format("{\"type\":\"delete\",\"id\":%d}", messageId);
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(deleteEvent));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

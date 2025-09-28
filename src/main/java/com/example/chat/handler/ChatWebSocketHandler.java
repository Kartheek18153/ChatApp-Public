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

        // Send existing messages to newly connected user
        messageRepository.findAll().forEach(msg -> {
            try {
                session.sendMessage(new TextMessage(
                        String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                                msg.getUsername(), msg.getContent(), msg.getTimestamp())
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        // Simple JSON parsing
        String username = payload.split("\"username\":\"")[1].split("\"")[0];
        String content = payload.split("\"content\":\"")[1].split("\"")[0];

        // SYSTEM message if user joined chat
        if(content.startsWith("__joined__")) {
            String userJoined = content.replace("__joined__", "");
            String systemMsg = String.format("{\"username\":\"SYSTEM\",\"content\":\"%s joined the chat\",\"timestamp\":\"%s\"}",
                    userJoined, Instant.now());
            broadcast(systemMsg);
            return;
        }

        // Save normal message
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        messageRepository.save(msg);

        String broadcast = String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                msg.getUsername(), msg.getContent(), msg.getTimestamp());

        broadcast(broadcast);
    }

    private void broadcast(String message) {
        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

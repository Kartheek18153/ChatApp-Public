package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    // List of banned words (expandable)
    private static final Set<String> BAD_WORDS = Set.of(
            "shit", "dumbass", "bitch", "fuck", "asshole", "bastard", "idiot"
    );

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        // Send existing chat history
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

        // Extract username and content
        String username = extractField(payload, "username");
        String content = extractField(payload, "content");

        // Save and broadcast the user message
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        messageRepository.save(msg);

        String broadcastMsg = String.format(
                "{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                msg.getUsername(), msg.getContent(), msg.getTimestamp()
        );
        broadcast(broadcastMsg);

        // Check for bad words after broadcasting the user message
        if (containsBadWords(content)) {
            String warning = String.format(
                    "{\"username\":\"SYSTEM\",\"content\":\"⚠️ Someone used inappropriate language!\",\"timestamp\":\"%s\"}",
                    Instant.now()
            );
            broadcast(warning);
        }
    }

    private boolean containsBadWords(String text) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        for (String bad : BAD_WORDS) {
            if (lowerText.contains(bad)) {
                return true;
            }
        }
        return false;
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

    private String extractField(String json, String field) {
        try {
            return json.split("\"" + field + "\":\"")[1].split("\"")[0];
        } catch (Exception e) {
            return "";
        }
    }
}

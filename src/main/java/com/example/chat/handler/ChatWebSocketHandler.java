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

        // Send existing messages
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

        String username = payload.split("\"username\":\"")[1].split("\"")[0];
        String content = payload.split("\"content\":\"")[1].split("\"")[0];

        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        messageRepository.save(msg);

        String broadcast = String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                msg.getUsername(), msg.getContent(), msg.getTimestamp());

        for (WebSocketSession s : sessions) {
            if (s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }

    // Broadcast system message
    public void broadcastSystemMessage(String content) {
        String timestamp = Instant.now().toString();
        String broadcast = String.format("{\"username\":\"SYSTEM\",\"content\":\"%s\",\"timestamp\":\"%s\"}", content, timestamp);

        for (WebSocketSession s : sessions) {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(broadcast));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

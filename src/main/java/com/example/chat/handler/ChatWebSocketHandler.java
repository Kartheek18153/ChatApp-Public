package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.UserPunishmentService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final UserPunishmentService punishmentService;

    // Store active sessions for broadcasting messages
    private final Map<String, WebSocketSession> activeSessions = new HashMap<>();

    // Basic inappropriate word list
    private final Set<String> bannedWords = new HashSet<>(Arrays.asList(
            "asshole", "bastard", "fuck", "piss", "bitch", "bollocks", "shit",
            "bloody", "bugger", "damn", "cock", "cunt", "wanker", "arsehole",
            "cocksucker", "motherfucker", "crappity", "hell", "rubbish", "shag", "son of"
    ));

    public ChatWebSocketHandler(MessageRepository messageRepository,
                                UserPunishmentService punishmentService) {
        this.messageRepository = messageRepository;
        this.punishmentService = punishmentService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String username = getUsername(session);
        activeSessions.put(username, session);
        System.out.println("✅ " + username + " connected to chat.");
        broadcast("🟢 " + username + " joined the chat.");
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        String username = getUsername(session);
        String text = message.getPayload().trim();

        // Check if user is muted
        if (punishmentService.isUserMuted(username)) {
            sendPrivateMessage(session, "🚫 You are muted. Wait until your mute expires.");
            return;
        }

        // Check for inappropriate content
        if (containsBannedWord(text)) {
            int strikes = punishmentService.addStrike(username);

            if (strikes >= 3) {
                punishmentService.muteUser(username, 3); // 🔥 MUTE FOR 3 MINUTES
                sendPrivateMessage(session, "❌ You have been muted for 3 minutes due to repeated violations.");
                broadcast("🔇 User " + username + " has been muted for 3 minutes due to inappropriate language.");
            } else {
                sendPrivateMessage(session, "⚠️ Inappropriate language detected. Strike " + strikes + "/3.");
            }
            return;
        }

        // Save valid message
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(text);
        msg.setTimestamp(Instant.now()); // ✅ Matches Message.java
        messageRepository.save(msg);

        // Broadcast message
        broadcast(username + ": " + text);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String username = getUsername(session);
        activeSessions.remove(username);
        broadcast("🔴 " + username + " left the chat.");
    }

    private void broadcast(String message) {
        activeSessions.values().forEach(session -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void sendPrivateMessage(WebSocketSession session, String message) throws IOException {
        session.sendMessage(new TextMessage(message));
    }

    private boolean containsBannedWord(String text) {
        String lower = text.toLowerCase();
        for (String bad : bannedWords) {
            if (lower.contains(bad)) return true;
        }
        return false;
    }

    private String getUsername(WebSocketSession session) {
        // Expect username as a query parameter: ws://localhost:8080/chat?username=Kartheek
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query != null && query.startsWith("username=")) {
            return query.substring("username=".length());
        }
        return "UnknownUser";
    }
}

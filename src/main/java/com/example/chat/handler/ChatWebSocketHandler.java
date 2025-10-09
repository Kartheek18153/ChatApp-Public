package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.service.UserPunishmentService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.Instant;
import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageRepository messageRepository;
    private final UserPunishmentService punishmentService;
    private final List<WebSocketSession> sessions = new ArrayList<>();

    // List of banned words
    private static final Set<String> BAD_WORDS = Set.of(
            "arsehole","asshole","a**hole","a$$hole","bastard","bitch","b*tch","b!tch",
            "bloody","bollocks","boobs","bugger","cheese and crackers","cock","cocksucker",
            "crap","crappity","cunt","damn","dick","dumb ass","dumbass","f***","f u c k",
            "fuck","f*ck","fuk","hell","hoe","how to use shit","idiot","jerk","mf","mfer",
            "mofo","moron","motherfucker","nigger","nigga","piss","prick","pussy","retard",
            "rubbish","sh1t","shag","shit","s***","slut","son of a bitch","stupid","tits",
            "twat","wanker","whore"
    );

    public ChatWebSocketHandler(MessageRepository messageRepository, UserPunishmentService punishmentService) {
        this.messageRepository = messageRepository;
        this.punishmentService = punishmentService;
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

        // If user is muted
        if (punishmentService.isUserMuted(username)) {
            sendPrivateMessage(session, "🚫 You are muted. Wait until your mute expires.");
            return;
        }

        // Check for bad words
        if (containsBadWords(content)) {
            int strikes = punishmentService.addStrike(username); // add a strike

            if (strikes >= 3) {
                punishmentService.muteUser(username, 3); // 3-minute mute
                broadcast(String.format("🔇 User %s has been muted for 3 minutes due to repeated violations.", username));
            } else {
                broadcast(String.format("⚠️ %s used inappropriate language! Strike %d/3.", username, strikes));
            }
            return; // don't save bad message
        }

        // Save and broadcast normal message
        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(Instant.now());
        messageRepository.save(msg);

        String broadcastMsg = String.format("{\"username\":\"%s\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                msg.getUsername(), msg.getContent(), msg.getTimestamp());
        broadcast(broadcastMsg);
    }

    private boolean containsBadWords(String text) {
        if (text == null) return false;
        String lowerText = text.toLowerCase();
        for (String bad : BAD_WORDS) {
            if (lowerText.contains(bad)) return true;
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

    private void sendPrivateMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) session.sendMessage(new TextMessage(
                    String.format("{\"username\":\"SYSTEM\",\"content\":\"%s\",\"timestamp\":\"%s\"}",
                            message, Instant.now())
            ));
        } catch (Exception e) {
            e.printStackTrace();
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

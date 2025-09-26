package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.model.BadWord;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.BadWordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new ArrayList<>();
    private final MessageRepository messageRepository;
    private final BadWordRepository badWordRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(MessageRepository messageRepository, BadWordRepository badWordRepository) {
        this.messageRepository = messageRepository;
        this.badWordRepository = badWordRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Convert JSON message to Map
        Map<String,Object> msg = mapper.readValue(message.getPayload(), Map.class);
        String content = ((String) msg.get("content")).toLowerCase(); // normalize

        // Fetch all bad words from DB
        List<String> badWords = badWordRepository.findAll()
                                                 .stream()
                                                 .map(BadWord::getWord)
                                                 .toList();

        boolean containsBadWord = false;
        for(String word : badWords){
            if(content.contains(word)) {
                containsBadWord = true;
                break;
            }
        }

        msg.put("timestamp", System.currentTimeMillis());

        if(containsBadWord){
            // Send alert to sender
            Map<String,Object> alert = new HashMap<>();
            alert.put("username", "System");
            alert.put("content", "⚠️ Your message contains prohibited words!");
            alert.put("timestamp", System.currentTimeMillis());
            session.sendMessage(new TextMessage(mapper.writeValueAsString(alert)));
            return; // Stop broadcasting bad message
        }

        // Save clean message to DB
        Message dbMsg = new Message();
        dbMsg.setUsername((String) msg.get("username"));
        dbMsg.setContent(content);
        dbMsg.setTimestamp((Long) msg.get("timestamp"));
        messageRepository.save(dbMsg);

        // Broadcast to all sessions
        String broadcast = mapper.writeValueAsString(msg);
        for(WebSocketSession s : sessions) {
            if(s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}

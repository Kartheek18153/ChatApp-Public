package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.model.BadWord;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.BadWordRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final MessageRepository messageRepository;
    private final BadWordRepository badWordRepository;
    private final Set<WebSocketSession> sessions = new HashSet<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatWebSocketHandler(MessageRepository messageRepository, BadWordRepository badWordRepository){
        this.messageRepository = messageRepository;
        this.badWordRepository = badWordRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session){
        sessions.add(session);
        try {
            // Send previous messages
            List<Message> allMessages = messageRepository.findAllByOrderByTimestampAsc();
            for(Message msg : allMessages){
                session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message){
        try {
            Map<String,String> payload = mapper.readValue(message.getPayload(), Map.class);
            String username = payload.get("username");
            String content = payload.get("content");

            // Check for bad words
            boolean containsBadWord = false;
            for(BadWord bw : badWordRepository.findAll()){
                if(content.toLowerCase().contains(bw.getWord().toLowerCase())){
                    containsBadWord = true;
                    break;
                }
            }

            if(containsBadWord){
                // Broadcast SYSTEM alert
                Message sysMsg = new Message("SYSTEM", username + " used a prohibited word!", LocalDateTime.now());
                broadcast(sysMsg);
                messageRepository.save(sysMsg);
            } else {
                Message msg = new Message(username, content, LocalDateTime.now());
                messageRepository.save(msg);
                broadcast(msg);
            }
        } catch (Exception e){ e.printStackTrace(); }
    }

    private void broadcast(Message msg){
        try{
            String json = mapper.writeValueAsString(msg);
            for(WebSocketSession s : sessions){
                s.sendMessage(new TextMessage(json));
            }
        } catch(Exception e){ e.printStackTrace(); }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status){
        sessions.remove(session);
    }
}

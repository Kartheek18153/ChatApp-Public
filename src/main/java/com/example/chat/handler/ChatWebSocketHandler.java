package com.example.chat.handler;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.example.chat.repository.MessageRepository;
import com.example.chat.model.Message;

import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private List<WebSocketSession> sessions = new ArrayList<>();

    @Autowired
    private MessageRepository messageRepository;

    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Map<String,Object> map = mapper.readValue(textMessage.getPayload(), Map.class);
        String username = (String) map.get("username");
        String content = (String) map.get("content");

        Message msg = new Message(username, content, System.currentTimeMillis());
        messageRepository.save(msg);

        String json = mapper.writeValueAsString(msg);
        for(WebSocketSession s: sessions) {
            s.sendMessage(new TextMessage(json));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session);
    }
}

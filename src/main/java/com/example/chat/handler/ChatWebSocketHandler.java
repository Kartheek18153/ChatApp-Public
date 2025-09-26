package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MessageRepository messageRepository;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);

        // Send previous messages
        List<Message> history = messageRepository.findAllByOrderByTimestampAsc();
        for(Message m : history) {
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
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String,Object> msg = mapper.readValue(message.getPayload(), Map.class);
        msg.put("timestamp", System.currentTimeMillis());

        // Save to DB
        Message dbMsg = new Message();
        dbMsg.setUsername((String) msg.get("username"));
        dbMsg.setContent((String) msg.get("content"));
        dbMsg.setTimestamp((Long) msg.get("timestamp"));
        messageRepository.save(dbMsg);

        String broadcast = mapper.writeValueAsString(msg);
        for(WebSocketSession s : sessions) {
            if(s.isOpen()) s.sendMessage(new TextMessage(broadcast));
        }
    }
}

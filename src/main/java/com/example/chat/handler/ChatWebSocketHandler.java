package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final ObjectMapper mapper = new ObjectMapper();
    private final MessageRepository repo;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public ChatWebSocketHandler(MessageRepository repo) {
        this.repo = repo;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Message msg = mapper.readValue(message.getPayload(), Message.class);
        repo.save(msg);
        String json = mapper.writeValueAsString(msg);
        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage(json));
        }
    }
}

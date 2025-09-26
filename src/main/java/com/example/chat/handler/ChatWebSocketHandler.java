package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private final MessageRepository messageRepository;

    public ChatWebSocketHandler(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
        broadcast(new Message("SYSTEM", "A user joined the chat"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage textMessage) throws Exception {
        Message msg = mapper.readValue(textMessage.getPayload(), Message.class);
        messageRepository.save(msg);
        broadcast(msg);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
        broadcast(new Message("SYSTEM", "A user left the chat"));
    }

    private void broadcast(Message msg) {
        try {
            String json = mapper.writeValueAsString(msg);
            for (WebSocketSession sess : sessions) {
                sess.sendMessage(new TextMessage(json));
            }
        } catch (Exception ignored) {}
    }
}

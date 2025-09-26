package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, String> msgMap = mapper.readValue(message.getPayload(), Map.class);

        Message msg = new Message();
        msg.setUsername(msgMap.get("username"));
        msg.setContent(msgMap.get("content"));
        msg.setTimestamp(LocalDateTime.now());

        messageRepository.save(msg);

        session.sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
    }
}

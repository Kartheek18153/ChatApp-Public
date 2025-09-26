package com.example.chat.handler;

import com.example.chat.model.Message;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.BadWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private BadWordRepository badWordRepository;

    private final List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        for (Message m : messageRepository.findAll()) {
            session.sendMessage(new TextMessage("{\"username\":\"" + m.getUsername() + "\",\"content\":\"" + m.getContent() + "\",\"timestamp\":" + m.getTimestamp() + "}"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        String username = payload.split("\"username\":\"")[1].split("\"")[0];
        String content = payload.split("\"content\":\"")[1].split("\"")[0];

        boolean hasBadWord = badWordRepository.findAll().stream()
                .anyMatch(b -> content.toLowerCase().contains(b.getWord().toLowerCase()));

        if (hasBadWord) {
            session.sendMessage(new TextMessage("{\"username\":\"SYSTEM\",\"content\":\"Message contains a banned word!\",\"timestamp\":" + System.currentTimeMillis() + "}"));
            return;
        }

        Message msg = new Message();
        msg.setUsername(username);
        msg.setContent(content);
        msg.setTimestamp(System.currentTimeMillis());
        messageRepository.save(msg);

        for (WebSocketSession s : sessions) {
            s.sendMessage(new TextMessage("{\"username\":\"" + username + "\",\"content\":\"" + content + "\",\"timestamp\":" + System.currentTimeMillis() + "}"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }
}

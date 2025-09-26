package com.example.chat.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="message")
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String content;
    private LocalDateTime timestamp = LocalDateTime.now();

    public Message() {}
    public Message(String username, String content) {
        this.username = username;
        this.content = content;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public LocalDateTime getTimestamp() { return timestamp; }

    public void setUsername(String username) { this.username = username; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}

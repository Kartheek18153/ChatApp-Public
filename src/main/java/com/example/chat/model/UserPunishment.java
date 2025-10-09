package com.example.chat.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "user_punishments")
public class UserPunishment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private int strikes;

    private Instant muteUntil; // null = not muted

    public UserPunishment() {}

    public UserPunishment(String username) {
        this.username = username;
        this.strikes = 0;
        this.muteUntil = null;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getStrikes() { return strikes; }
    public void setStrikes(int strikes) { this.strikes = strikes; }

    public Instant getMuteUntil() { return muteUntil; }
    public void setMuteUntil(Instant muteUntil) { this.muteUntil = muteUntil; }
}

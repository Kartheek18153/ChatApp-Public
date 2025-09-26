package com.example.chat.model;

import jakarta.persistence.*;

@Entity
@Table(name="badwords")
public class BadWord {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(unique=true)
    private String word;

    public BadWord() {}
    public BadWord(String word) { this.word = word; }

    public Long getId() { return id; }
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
}

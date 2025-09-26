package com.example.chat.model;

import jakarta.persistence.*;

@Entity
@Table(name = "badwords", uniqueConstraints = {@UniqueConstraint(columnNames = {"word"})})
public class BadWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String word;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}

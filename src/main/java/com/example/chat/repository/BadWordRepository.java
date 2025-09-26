package com.example.chat.repository;

import com.example.chat.model.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadWordRepository extends JpaRepository<BadWord, Long> {
}

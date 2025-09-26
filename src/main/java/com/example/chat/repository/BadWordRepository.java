package com.example.chat.repository;

import com.example.chat.model.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BadWordRepository extends JpaRepository<BadWord, Long> {

    // This method allows us to check if a word already exists
    boolean existsByWord(String word);
}

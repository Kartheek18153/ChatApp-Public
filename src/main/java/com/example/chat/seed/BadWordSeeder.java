package com.example.chat.seed;

import com.example.chat.model.BadWord;
import com.example.chat.repository.BadWordRepository;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class BadWordSeeder {
    private final BadWordRepository badWordRepository;

    public BadWordSeeder(BadWordRepository badWordRepository) {
        this.badWordRepository = badWordRepository;
    }

    @PostConstruct
    public void seed() throws Exception {
        if(badWordRepository.count() > 0) return;

        try(BufferedReader br = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/badwords.txt")))) {
            String line;
            while((line = br.readLine()) != null) {
                String word = line.trim().toLowerCase();
                if(!word.isEmpty() && !badWordRepository.existsByWord(word)) {
                    badWordRepository.save(new BadWord(word));
                }
            }
        }
    }
}

package com.example.chat.seed;

import com.example.chat.model.BadWord;
import com.example.chat.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class BadWordSeeder {
    private final BadWordRepository badWordRepository;

    public BadWordSeeder(BadWordRepository badWordRepository) {
        this.badWordRepository = badWordRepository;
    }

    @PostConstruct
    public void seed() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/badwords.txt")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if(!line.isEmpty() && !badWordRepository.existsByWord(line)){
                    badWordRepository.save(new BadWord(line));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

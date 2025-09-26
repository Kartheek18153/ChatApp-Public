package com.example.chat.seed;

import com.example.chat.model.BadWord;
import com.example.chat.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class BadWordSeeder {

    @Autowired
    private BadWordRepository badWordRepository;

    @PostConstruct
    public void seed() throws Exception {
        if (badWordRepository.count() == 0) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(getClass().getClassLoader().getResourceAsStream("badwords.txt")))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.isBlank()) {
                        BadWord bw = new BadWord();
                        bw.setWord(line.trim());
                        badWordRepository.save(bw);
                    }
                }
            }
        }
    }
}

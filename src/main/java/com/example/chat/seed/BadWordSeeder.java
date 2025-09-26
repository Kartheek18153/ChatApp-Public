package com.example.chat.seed;

import com.example.chat.model.BadWord;
import com.example.chat.repository.BadWordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class BadWordSeeder {

    @Autowired
    private BadWordRepository badWordRepository;

    @PostConstruct
    public void seed() {
        try (InputStream is = getClass().getResourceAsStream("/badwords.txt")) {
            if (is == null) {
                System.out.println("BadWordSeeder: badwords.txt not found in resources!");
                return;
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                int count = 0;

                while ((line = reader.readLine()) != null) {
                    line = line.trim().toLowerCase();

                    if (!line.isEmpty() && !badWordRepository.existsByWord(line)) {
                        BadWord bw = new BadWord();
                        bw.setWord(line);
                        badWordRepository.save(bw);
                        count++;
                    }
                }

                System.out.println("BadWordSeeder: Seeded " + count + " bad words into database.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

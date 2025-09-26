package com.example.chat.seed;

import com.example.chat.model.BadWord;
import com.example.chat.repository.BadWordRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class BadWordSeeder {

    @Autowired
    private BadWordRepository badWordRepository;

    @PostConstruct
    public void seed() {
        if(badWordRepository.count() > 0) return;

        try(BufferedReader br = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream("/badwords.txt")))) {

            String line;
            while((line = br.readLine()) != null){
                BadWord bw = new BadWord();
                bw.setWord(line.trim());
                badWordRepository.save(bw);
            }

        } catch(Exception e){
            e.printStackTrace();
        }
    }
}

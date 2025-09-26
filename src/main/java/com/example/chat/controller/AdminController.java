package com.example.chat.controller;

import com.example.chat.model.User;
import com.example.chat.repository.MessageRepository;
import com.example.chat.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @DeleteMapping("/deleteAllMessages")
    public Map<String, String> deleteAllMessages(@RequestParam String username) {
        User user = userRepository.findByUsername(username);
        Map<String, String> res = new HashMap<>();

        if (user == null || !"ADMIN".equals(user.getRole())) {
            res.put("status", "failed");
            res.put("message", "You are not authorized!");
            return res;
        }

        messageRepository.deleteAll();
        res.put("status", "success");
        res.put("message", "All messages deleted!");
        return res;
    }
}

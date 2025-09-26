package com.example.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chat.model.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {}

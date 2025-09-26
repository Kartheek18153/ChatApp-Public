package com.example.chat.repository;

import com.example.chat.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    // Fetch last 50 messages in ascending order
    List<Message> findTop50ByOrderByTimestampAsc();

    // Optional: delete all messages (for admin endpoint)
    void deleteAll();
}

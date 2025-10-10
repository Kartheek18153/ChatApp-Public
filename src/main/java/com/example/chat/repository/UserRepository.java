package com.example.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.chat.model.User;

public interface UserRepository extends JpaRepository<User, String> {
    User findByUsername(String username);
}

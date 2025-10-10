
package com.example.chat.repository;

import com.example.chat.model.UserPunishment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserPunishmentRepository extends JpaRepository<UserPunishment, Long> {
    Optional<UserPunishment> findByUsername(String username);
}

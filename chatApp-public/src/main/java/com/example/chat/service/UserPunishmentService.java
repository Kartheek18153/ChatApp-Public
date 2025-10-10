
package com.example.chat.service;

import com.example.chat.model.UserPunishment;
import com.example.chat.repository.UserPunishmentRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class UserPunishmentService {

    private final UserPunishmentRepository repository;

    public UserPunishmentService(UserPunishmentRepository repository) {
        this.repository = repository;
    }

    // Adds a strike and returns total count
    public int addStrike(String username) {
        UserPunishment user = repository.findByUsername(username)
                .orElseGet(() -> new UserPunishment(username));

        // If the user is currently muted, reset strikes when unmuted
        if (isUserMuted(username)) return user.getStrikes();

        user.setStrikes(user.getStrikes() + 1);
        repository.save(user);
        return user.getStrikes();
    }

    // Check if user is muted
    public boolean isUserMuted(String username) {
        return repository.findByUsername(username)
                .map(u -> u.getMuteUntil() != null && Instant.now().isBefore(u.getMuteUntil()))
                .orElse(false);
    }

    // Mute user for given minutes
    public void muteUser(String username, int minutes) {
        UserPunishment user = repository.findByUsername(username)
                .orElseGet(() -> new UserPunishment(username));

        user.setMuteUntil(Instant.now().plus(minutes, ChronoUnit.MINUTES));
        user.setStrikes(0); // reset strikes after mute
        repository.save(user);
    }

    // Optional: get remaining mute time
    public long getRemainingMuteSeconds(String username) {
        return repository.findByUsername(username)
                .filter(u -> u.getMuteUntil() != null)
                .map(u -> Instant.now().isBefore(u.getMuteUntil())
                        ? u.getMuteUntil().getEpochSecond() - Instant.now().getEpochSecond()
                        : 0L)
                .orElse(0L);
    }
}

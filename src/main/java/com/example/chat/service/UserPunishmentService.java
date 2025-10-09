package com.example.chat.service;

import com.example.chat.model.UserPunishment;
import com.example.chat.repository.UserPunishmentRepository;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class UserPunishmentService {

    private final UserPunishmentRepository repository;

    // Change these if you want to tweak behavior
    private static final int MAX_STRIKES = 3;
    private static final int MUTE_DURATION_SECONDS = 300; // 5 minutes

    public UserPunishmentService(UserPunishmentRepository repository) {
        this.repository = repository;
    }

    public boolean isMuted(String username) {
        UserPunishment punishment = getOrCreate(username);
        Instant muteUntil = punishment.getMuteUntil();

        if (muteUntil == null) return false;

        // If mute time expired, reset
        if (Instant.now().isAfter(muteUntil)) {
            punishment.setMuteUntil(null);
            punishment.setStrikes(0);
            repository.save(punishment);
            return false;
        }
        return true;
    }

    public void addStrike(String username) {
        UserPunishment punishment = getOrCreate(username);
        punishment.setStrikes(punishment.getStrikes() + 1);

        if (punishment.getStrikes() >= MAX_STRIKES) {
            punishment.setMuteUntil(Instant.now().plusSeconds(MUTE_DURATION_SECONDS));
        }

        repository.save(punishment);
    }

    public int getStrikeCount(String username) {
        return getOrCreate(username).getStrikes();
    }

    public long getMuteRemainingSeconds(String username) {
        UserPunishment punishment = getOrCreate(username);
        if (punishment.getMuteUntil() == null) return 0;
        return Math.max(0, punishment.getMuteUntil().getEpochSecond() - Instant.now().getEpochSecond());
    }

    private UserPunishment getOrCreate(String username) {
        return repository.findByUsername(username)
                .orElseGet(() -> repository.save(new UserPunishment(username)));
    }
}

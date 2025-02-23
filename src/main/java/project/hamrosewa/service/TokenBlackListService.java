package project.hamrosewa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import project.hamrosewa.util.JWTUtil;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlackListService {

    private final ConcurrentHashMap<String, Date> blacklistedTokens = new ConcurrentHashMap<>();

    @Autowired
    private JWTUtil jwtUtil;

    public void blacklistToken(String token) {
        if (!blacklistedTokens.containsKey(token)) {
            Date expiration = jwtUtil.extractExpiration(token);
            blacklistedTokens.put(token, expiration);
        }
    }

    public boolean isBlacklisted(String token) {
        return blacklistedTokens.containsKey(token);
    }

    @Scheduled(fixedRate = 3600000) // Cleanup every hour
    public void cleanupExpiredTokens() {
        Date now = new Date();
        blacklistedTokens.entrySet().removeIf(entry ->
                entry.getValue().before(now));
    }
}

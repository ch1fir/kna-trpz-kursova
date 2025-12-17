package com.example.mindmap.soa.server;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TokenService {

    private static final class Session {
        final int userId;
        final long expiresAtEpochSec;
        Session(int userId, long expiresAtEpochSec) {
            this.userId = userId;
            this.expiresAtEpochSec = expiresAtEpochSec;
        }
    }

    private final SecureRandom rng = new SecureRandom();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final long ttlSeconds;

    public TokenService(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public String issueToken(int userId) {
        byte[] buf = new byte[32];
        rng.nextBytes(buf);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
        long exp = Instant.now().getEpochSecond() + ttlSeconds;
        sessions.put(token, new Session(userId, exp));
        return token;
    }

    public Integer validate(String token) {
        if (token == null || token.isBlank()) return null;
        Session s = sessions.get(token);
        if (s == null) return null;
        if (Instant.now().getEpochSecond() > s.expiresAtEpochSec) {
            sessions.remove(token);
            return null;
        }
        return s.userId;
    }
}

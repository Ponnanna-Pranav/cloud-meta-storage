package com.cloudstorage.backend.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // ⚠️ Must be at least 256 bits for HS256
    private static final String SECRET =
            "THIS_IS_A_VERY_LONG_SECRET_KEY_FOR_JWT_SIGNING_123456";

    private static final long EXPIRATION =
            1000 * 60 * 60 * 24; // 24 hours

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes());

    // ✅ CREATE TOKEN
    public String generateToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(key)
                .compact();
    }

    // ✅ EXTRACT EMAIL
    public String extractEmail(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    // ✅ VALIDATE TOKEN (THIS FIXES 403 + FILTER ISSUES)
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}

package com.nudgeme.nudgeme.security;

import com.nudgeme.nudgeme.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // At least 32 characters for HS256
    private final String SECRET_KEY = "your_very_long_secret_key_that_is_at_least_32_chars";

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    }

    // 🔹 Extract UUID (subject) from token
    public String extractUuid(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 🔹 Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }


    // 🔹 Extract expiration date
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // 🔹 Generic method to extract any claim
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 🔹 Generate JWT Token with UUID
    public String generateToken(User user) { // pass User instead of UUID
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        claims.put("role", user.getRole());
        claims.put("email", user.getEmail());
        return createToken(claims, user.getUuid());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject) // UUID
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 hours
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    // 🔹 Validate token by UUID
    public boolean validateToken(String token, String uuid) {
        final String extractedUuid = extractUuid(token);
        return (extractedUuid.equals(uuid) && !isTokenExpired(token));
    }

    // 🔹 Check expiration
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // 🔹 Get all claims
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Password Reset Token
    public String generateResetToken(User user) {
        long expiration = 15 * 60 * 1000; // 15 minutes
        SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

}

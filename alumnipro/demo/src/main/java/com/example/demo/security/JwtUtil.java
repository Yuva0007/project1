package com.example.demo.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Use a proper secret key - should be at least 256 bits (32 bytes) for HS256
    private final String SECRET_KEY = "mySecretKeyThatIsLongEnoughForHS256Algorithm123456789";
    private final SecretKey key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    
    // Token validity - 10 hours
    private final long JWT_TOKEN_VALIDITY = 10 * 60 * 60 * 1000; // 10 hours
    
    // Refresh token validity - 7 days
    private final long REFRESH_TOKEN_VALIDITY = 7 * 24 * 60 * 60 * 1000; // 7 days

    // Generate access token with user details
    public String generateToken(String username, String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("role", role);
        claims.put("username", username);
        return createToken(claims, username, JWT_TOKEN_VALIDITY);
    }

    // Generate refresh token
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return createToken(claims, username, REFRESH_TOKEN_VALIDITY);
    }

    // Create token with custom validity
    private String createToken(Map<String, Object> claims, String subject, long validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract username from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract email from token (custom claim)
    public String extractEmail(String token) {
        try {
            return extractClaim(token, claims -> (String) claims.get("email"));
        } catch (Exception e) {
            return null;
        }
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Extract issued at date from token
    public Date extractIssuedAt(String token) {
        return extractClaim(token, Claims::getIssuedAt);
    }

    // Extract specific claim from token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Extract all claims from token
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            // Return expired claims so we can check expiration later
            return e.getClaims();
        } catch (JwtException e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    // Check if token is expired
    public Boolean isTokenExpired(String token) {
        try {
            final Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (Exception e) {
            return true; // Consider invalid tokens as expired
        }
    }

    // Validate token
    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    // Validate token without username check
    public Boolean validateToken(String token) {
        try {
            Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Get token type (access or refresh)
    public String getTokenType(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return (String) claims.getOrDefault("type", "access");
        } catch (Exception e) {
            return "invalid";
        }
    }

    // Calculate remaining time until expiration
    public long getRemainingTime(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.getTime() - System.currentTimeMillis();
        } catch (Exception e) {
            return 0;
        }
    }
}

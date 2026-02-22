package com.harmoni.pos.order.component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

/**
 * {@code JwtUtil} is a utility component for generating, validating,
 * and parsing JWT tokens used for authenticating users in the POS system.
 */
@Getter
@Component
public class JwtUtil {

    /** Default expiration time (in milliseconds) if not overridden: 1 day. */
    private static final long EXPIRATION_TIME = 86400000;

    /** Secret key for signing the JWT, injected from configuration. */
    @Value("${harmoni.order.jwt.secret}")
    private String secretKey;

    /** Expiration time (in milliseconds) for JWT tokens, injected from configuration. */
    @Value("${harmoni.order.jwt.expired.time}")
    private long expiredTime;

    /**
     * Generates a JWT token for a given username.
     *
     * @param username the username to include in the JWT subject
     * @return a signed JWT string
     */
    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expiredTime)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .compact();
    }

    /**
     * Extracts the username (subject) from the JWT token.
     *
     * @param token the JWT string
     * @return the username extracted from the token
     */
    public String extractUsername(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    /**
     * Validates the JWT token against the provided username and checks for expiration.
     *
     * @param token    the JWT string
     * @param username the username to match with the token subject
     * @return {@code true} if valid and not expired, {@code false} otherwise
     */
    public boolean validateToken(String token, String username) {
        return extractUsername(token).equals(username) && !isTokenExpired(token);
    }

    /**
     * Checks whether the JWT token is expired.
     *
     * @param token the JWT string
     * @return {@code true} if the token has expired, {@code false} otherwise
     */
    private boolean isTokenExpired(String token) {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey)))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .before(new Date());
    }

    /**
     * Extracts the JWT token from a Bearer authorization header.
     *
     * @param authorization the full authorization header (e.g., "Bearer eyJhb...")
     * @return the JWT token string without the "Bearer " prefix
     */
    public String getTokenFromBearer(String authorization) {
        return authorization.substring(7);
    }
}

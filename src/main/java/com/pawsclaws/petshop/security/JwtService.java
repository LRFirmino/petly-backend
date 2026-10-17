package com.pawsclaws.petshop.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * Creates and validates signed JWTs (HS256).
 *
 * <p>The token carries the tenant id ({@code petshopId}) as a claim. It is
 * signed, so a client cannot tamper with it to impersonate another petshop.</p>
 */
@Service
public class JwtService {

    private static final String CLAIM_PETSHOP_ID = "petshopId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_EMAIL = "email";

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(UserPrincipal principal) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(String.valueOf(principal.getId()))
                .claims(Map.of(
                        CLAIM_PETSHOP_ID, principal.getPetshopId(),
                        CLAIM_ROLE, principal.getRole().name(),
                        CLAIM_NAME, principal.getName(),
                        CLAIM_EMAIL, principal.getUsername()))
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * Parses and verifies the token. Throws {@link JwtException} when the
     * signature, structure or expiration is invalid.
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    public long getExpirationMs() {
        return expirationMs;
    }

    public static String claimPetshopId() {
        return CLAIM_PETSHOP_ID;
    }

    public static String claimRole() {
        return CLAIM_ROLE;
    }

    public static String claimName() {
        return CLAIM_NAME;
    }

    public static String claimEmail() {
        return CLAIM_EMAIL;
    }
}

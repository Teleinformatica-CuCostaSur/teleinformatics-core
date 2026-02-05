package edu.teleinformatics.core.security.jwt;

import edu.teleinformatics.core.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final JwtProperties jwtProperties;

    private SecretKey getSignInKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes());
    }

    public String generateToken(UUID id, String email, List<String> roles) {
        return Jwts.builder()
                .subject(id.toString())
                .claim("email", email)
                .claim("roles", roles)
                .issuedAt(new Date()) // Sets the token issuance time to current time
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getExpiration())) // Sets expiration time
                .signWith(getSignInKey(), Jwts.SIG.HS256) // Signs the token with HMAC SHA-256
                .compact(); // Builds and returns the compact JWT string
    }

    public <T> T extractClaim(String token, String claimName, Class<T> clazz) {
        return parseToken(token).get(claimName, clazz);
    }

    public boolean isTokenValid(String token) {
        return !parseToken(token).getExpiration().before(new Date());
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
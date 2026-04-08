package br.univille.mindflow.security;

import br.univille.mindflow.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Geração e validação de JWTs para autenticação stateless.
 * HS256 (HMAC-SHA256) com secret de pelo menos 256 bits.
 */
@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${mindflow.jwt.secret}") String secret,
                      @Value("${mindflow.jwt.expiration-ms}") long expirationMs) {
        // HS256 requer chave de >= 32 bytes. Pad com espaços se for menor.
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            for (int i = bytes.length; i < 32; i++) padded[i] = ' ';
            bytes = padded;
        }
        this.key = Keys.hmacShaKeyFor(bytes);
        this.expirationMs = expirationMs;
    }

    public String generate(User user) {
        Date now = new Date();
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("uid", user.getId())
                .claim("name", user.getName())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expirationMs))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return parse(token).getSubject();
    }

    public long getExpirationMs() { return expirationMs; }
}

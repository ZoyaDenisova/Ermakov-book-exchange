package org.bookswap.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.bookswap.auth.dto.TokenPairDto;
import org.bookswap.common.exception.UnauthorizedException;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtManager implements TokenManager {

    private final Key key = Keys.hmacShaKeyFor("AAAAAAAAAABBBBBBBBBBCCCCCCCCCCDDDDDDDDDD".getBytes(StandardCharsets.UTF_8));
    private final int accessExpirationMinutes = 15;
    private final int refreshExpirationDays = 7;

    @Override
    public TokenPairDto generate(long userId, String role) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime accessExpiry = now.plusMinutes(accessExpirationMinutes);
        LocalDateTime refreshExpiry = now.plusDays(refreshExpirationDays);

        String accessToken = Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role)
                .setIssuedAt(toDate(now))
                .setExpiration(toDate(accessExpiry))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        String refreshToken = UUID.randomUUID().toString();

        return new TokenPairDto(accessToken, refreshToken, accessExpiry, refreshExpiry);
    }

    @Override
    public ParsedToken validate(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Long userId = Long.valueOf(claims.getSubject());
            String role = claims.get("role", String.class);

            return new ParsedToken(userId, role);
        } catch (JwtException e) {
            throw new UnauthorizedException("Invalid or expired JWT token");
        }
    }

    private Date toDate(LocalDateTime dateTime) {
        return Date.from(dateTime.toInstant(ZoneOffset.UTC));
    }
}

package org.bookswap.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.bookswap.auth.security.TokenManager.ParsedToken;
import org.bookswap.common.exception.UnauthorizedException;
import org.springframework.http.HttpHeaders;

@Getter
public class AuthContext {
    private final Long userId;
    private final String role;

    public AuthContext(HttpServletRequest request, TokenManager tokenManager) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String token = header.substring("Bearer ".length());
        ParsedToken parsed = tokenManager.validate(token);
        this.userId = parsed.userId();
        this.role = parsed.role();
    }
}
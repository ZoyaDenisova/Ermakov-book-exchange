package org.bookswap.auth.security;

import org.bookswap.auth.dto.TokenPairDto;

public interface TokenManager {
    TokenPairDto generate(long userId, String role);
    ParsedToken validate(String token);

    record ParsedToken(Long userId, String role) {}
}
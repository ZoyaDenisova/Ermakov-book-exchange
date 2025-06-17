package org.bookswap.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.*;
import org.bookswap.auth.security.TokenManager.ParsedToken;
import org.bookswap.auth.usecase.AuthUseCase;
import org.bookswap.auth.security.TokenManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final TokenManager tokenManager;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.register(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.login(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                     HttpServletResponse response) {
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is missing");
        }

        TokenPairDto tokens = authUseCase.refreshToken(refreshToken);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokens.accessToken())
                .body(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                    HttpServletResponse response) {
        if (refreshToken != null) {
            authUseCase.logout(refreshToken);
            deleteRefreshTokenCookie(response);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<?> logoutAll(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                       HttpServletResponse response) {
        Long userId = extractUserId(authHeader);
        authUseCase.logoutAll(userId);
        deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("refreshToken", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Invalid Authorization header");
        }

        String token = authHeader.substring(7);
        ParsedToken parsed = tokenManager.validate(token);
        return parsed.userId();
    }
}
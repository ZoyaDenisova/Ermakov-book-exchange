package org.bookswap.auth.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.*;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.auth.security.TokenManager.ParsedToken;
import org.bookswap.auth.usecase.AuthUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final TokenManager tokenManager;

    @PostMapping("/register")
    public ResponseEntity<TokenPairDto> register(@RequestBody RegisterDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.register(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenPairDto> login(@RequestBody LoginDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.login(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken) {
        authUseCase.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        authUseCase.logoutAll(userId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenPairDto> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.refreshToken(refreshToken);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(authUseCase.getById(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<Void> update(@RequestHeader("Authorization") String authHeader,
                                       @RequestBody UpdateUserDto dto) {
        Long userId = extractUserId(authHeader);
        authUseCase.updateUser(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestHeader("Authorization") String authHeader,
                                               @RequestBody ChangePasswordDto dto) {
        Long userId = extractUserId(authHeader);
        authUseCase.changePassword(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/role/{id}")
    public ResponseEntity<Void> changeRole(@PathVariable Long id,
                                           @RequestBody ChangeRoleDto dto) {
        authUseCase.changeRole(id, dto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ban/{id}")
    public ResponseEntity<Void> banUser(@PathVariable Long id) {
        authUseCase.banUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/unban/{id}")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id) {
        authUseCase.unbanUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authUseCase.getById(id));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(authUseCase.getAll());
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDto>> getSessions(@RequestHeader("Authorization") String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(authUseCase.getUserSessions(userId));
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private Long extractUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        ParsedToken parsed = tokenManager.validate(token);
        return parsed.userId();
    }
}

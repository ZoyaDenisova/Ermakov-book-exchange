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

import java.util.List;

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

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(authUseCase.getById(userId));
    }

    @PatchMapping("/user")
    public ResponseEntity<Void> updateUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                           @RequestBody UpdateUserDto dto) {
        Long userId = extractUserId(authHeader);
        authUseCase.updateUser(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                               @RequestBody ChangePasswordDto dto) {
        Long userId = extractUserId(authHeader);
        authUseCase.changePassword(userId, dto);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/role/{id}")
    public ResponseEntity<Void> changeRole(@PathVariable Long id, @RequestBody ChangeRoleDto dto) {
        authUseCase.changeRole(id, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDto>> getSessions(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        Long userId = extractUserId(authHeader);
        return ResponseEntity.ok(authUseCase.getUserSessions(userId));
    }

    @DeleteMapping("/session")
    public ResponseEntity<Void> deleteSession(@CookieValue(name = "refreshToken", required = false) String refreshToken,
                                              HttpServletResponse response) {
        if (refreshToken != null) {
            authUseCase.logout(refreshToken);
            deleteRefreshTokenCookie(response);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/sessions")
    public ResponseEntity<Void> deleteAllSessions(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
                                                  HttpServletResponse response) {
        Long userId = extractUserId(authHeader);
        authUseCase.logoutAll(userId);
        deleteRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/block")
    public ResponseEntity<Void> blockUser(@PathVariable Long id) {
        authUseCase.banUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/users/{id}/unblock")
    public ResponseEntity<Void> unblockUser(@PathVariable Long id) {
        authUseCase.unbanUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(authUseCase.getAll());
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
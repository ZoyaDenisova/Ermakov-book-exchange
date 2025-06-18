package org.bookswap.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.*;
import org.bookswap.auth.entity.Role;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.auth.security.TokenManager.ParsedToken;
import org.bookswap.auth.usecase.AuthUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthUseCase authUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/register")
    public ResponseEntity<TokenPairDto> register(@RequestBody RegisterDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.register(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Аутентификация пользователя")
    @PostMapping("/login")
    public ResponseEntity<TokenPairDto> login(@RequestBody LoginDto dto, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.login(dto);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Выход с одного устройства")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue("refreshToken") String refreshToken) {
        authUseCase.logout(refreshToken);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Выход со всех устройств")
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        authUseCase.logoutAll(ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Обновление access/refresh токенов")
    @PostMapping("/refresh")
    public ResponseEntity<TokenPairDto> refresh(@CookieValue("refreshToken") String refreshToken, HttpServletResponse response) {
        TokenPairDto tokens = authUseCase.refreshToken(refreshToken);
        setRefreshTokenCookie(response, tokens.refreshToken());
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Получить текущего пользователя")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(authUseCase.getById(ctx.getUserId()));
    }

    @Operation(summary = "Обновить профиль текущего пользователя")
    @PatchMapping("/me")
    public ResponseEntity<Void> update(HttpServletRequest request,
                                       @RequestBody UpdateUserDto dto) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        authUseCase.updateUser(ctx.getUserId(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Сменить пароль")
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(HttpServletRequest request,
                                               @RequestBody ChangePasswordDto dto) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        authUseCase.changePassword(ctx.getUserId(), dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Изменить роль пользователя (ADMIN)")
    @PatchMapping("/role/{id}")
    public ResponseEntity<Void> changeRole(@PathVariable Long id,
                                           @RequestBody ChangeRoleDto dto,
                                           HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.ADMIN);
        authUseCase.changeRole(id, dto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Забанить пользователя (MODERATOR/ADMIN)")
    @PostMapping("/ban/{id}")
    public ResponseEntity<Void> banUser(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.MODERATOR, Role.ADMIN);
        authUseCase.banUser(id, ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Разбанить пользователя (MODERATOR/ADMIN)")
    @PostMapping("/unban/{id}")
    public ResponseEntity<Void> unbanUser(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        SecurityUtil.assertHasRole(ctx.getRole(), Role.MODERATOR, Role.ADMIN);
        authUseCase.unbanUser(id, ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить пользователя по ID")
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(authUseCase.getById(id));
    }

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAll() {
        return ResponseEntity.ok(authUseCase.getAll());
    }

    @Operation(summary = "Получить все активные сессии текущего пользователя")
    @GetMapping("/sessions")
    public ResponseEntity<List<SessionDto>> getSessions(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(authUseCase.getUserSessions(ctx.getUserId()));
    }

    @Operation(summary = "Загрузить или заменить аватар")
    @PutMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @RequestPart("file") MultipartFile file,
            HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        authUseCase.uploadAvatar(ctx.getUserId(), file);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удалить аватар")
    @DeleteMapping("/avatar")
    public ResponseEntity<Void> deleteAvatar(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        authUseCase.deleteAvatar(ctx.getUserId());
        return ResponseEntity.ok().build();
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

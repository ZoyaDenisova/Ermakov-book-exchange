package org.bookswap.exchange.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.exchange.dto.ExchangeCreateDto;
import org.bookswap.exchange.dto.ExchangeDto;
import org.bookswap.exchange.dto.ExchangeFilterDto;
import org.bookswap.exchange.entity.ExchangeStatus;
import org.bookswap.exchange.usecase.ExchangeUseCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/exchange")
@RequiredArgsConstructor
public class ExchangeController {

    private final ExchangeUseCase exchangeUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Предложить обмен")
    @PostMapping
    public ResponseEntity<ExchangeDto> proposeExchange(@RequestBody ExchangeCreateDto dto, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(exchangeUseCase.proposeExchange(ctx.getUserId(), dto));
    }

    @Operation(summary = "Принять обмен")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<Void> approveExchange(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        exchangeUseCase.approveExchange(id, ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отклонить обмен")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<Void> rejectExchange(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        exchangeUseCase.rejectExchange(id, ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Подтвердить завершение обмена")
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<Void> confirmExchangeCompletion(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        exchangeUseCase.confirmCompletion(id, ctx.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получить список обменов пользователя (с фильтром по статусу)")
    @GetMapping
    public ResponseEntity<Page<ExchangeDto>> getUserExchanges(
            @RequestParam(name = "status", required = false) ExchangeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        ExchangeFilterDto filter = new ExchangeFilterDto(status);
        Page<ExchangeDto> result = exchangeUseCase.getUserExchanges(ctx.getUserId(), filter, PageRequest.of(page, size));
        return ResponseEntity.ok(result);
    }

}
package org.bookswap.messaging.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.security.AuthContext;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.messaging.dto.DialogDto;
import org.bookswap.messaging.dto.MessageDto;
import org.bookswap.messaging.usecase.MessagingUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessagingController {

    private final MessagingUseCase messagingUseCase;
    private final TokenManager tokenManager;

    @Operation(summary = "Получить список диалогов текущего пользователя")
    @GetMapping("/dialogs")
    public ResponseEntity<List<DialogDto>> getUserDialogs(HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(messagingUseCase.getUserDialogs(ctx.getUserId()));
    }

    @Operation(summary = "Получить подробности конкретного диалога")
    @GetMapping("/dialogs/{id}")
    public ResponseEntity<DialogDto> getDialogDetails(@PathVariable Long id, HttpServletRequest request) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(messagingUseCase.getDialogDetails(id, ctx.getUserId()));
    }

    @Operation(summary = "Получить сообщения в диалоге")
    @GetMapping("/dialogs/{id}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        return ResponseEntity.ok(messagingUseCase.getMessages(id, ctx.getUserId(), page, size));
    }

//    @Operation(summary = "Создать диалог с владельцем объявления (без сообщений)")
//    @PostMapping("/listing/{listingId}/create-dialog")
//    public ResponseEntity<Long> createDialog(
//            @PathVariable Long listingId,
//            HttpServletRequest request
//    ) {
//        AuthContext ctx = new AuthContext(request, tokenManager);
//        Long dialogId = messagingUseCase.createDialogOnly(ctx.getUserId(), listingId);
//        return ResponseEntity.ok(dialogId);
//    }


    @Operation(summary = "Отправить сообщение по объявлению (текст и до 3 фото)")
    @PostMapping(value = "/listing/{listingId}/send", consumes = "multipart/form-data")
    public ResponseEntity<Void> sendMessage(
            @PathVariable Long listingId,
            @RequestPart("content") String content,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        messagingUseCase.sendMessage(ctx.getUserId(), listingId, content, images);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Отправить сообщение с предложением обмена")
    @PostMapping("/listing/{listingId}/propose-exchange")
    public ResponseEntity<Void> sendExchangeProposal(
            @PathVariable Long listingId,
            @RequestParam("offeredListingId") Long offeredListingId,
            HttpServletRequest request
    ) {
        AuthContext ctx = new AuthContext(request, tokenManager);
        messagingUseCase.sendExchangeProposal(ctx.getUserId(), listingId, offeredListingId);
        return ResponseEntity.ok().build();
    }
}
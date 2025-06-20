package org.bookswap.messaging.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.catalog.entity.Book;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.exchange.dto.ExchangeCreateDto;
import org.bookswap.exchange.entity.Exchange;
import org.bookswap.exchange.usecase.ExchangeUseCase;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.entity.ListingImage;
import org.bookswap.listings.repository.ListingImageRepo;
import org.bookswap.listings.repository.ListingRepo;
import org.bookswap.messaging.dto.DialogDto;
import org.bookswap.messaging.dto.MessageDto;
import org.bookswap.messaging.entity.ChatImage;
import org.bookswap.messaging.entity.Dialog;
import org.bookswap.messaging.entity.Message;
import org.bookswap.messaging.repository.ChatImageRepo;
import org.bookswap.messaging.repository.DialogRepo;
import org.bookswap.messaging.repository.MessageRepo;
import org.bookswap.shared.image.ImageService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class MessagingUseCase {

    private final DialogRepo dialogRepo;
    private final MessageRepo messageRepo;
    private final ChatImageRepo chatImageRepo;
    private final ListingRepo listingRepo;
    private final ListingImageRepo listingImageRepo;
    private final ExchangeUseCase exchangeUseCase;
    private final UserRepo userRepo;
    private final ImageService imageService;

    public List<DialogDto> getUserDialogs(Long userId) {
        return dialogRepo.findUserDialogsOrderedByLastMessage(userId).stream().map(dialog -> {
            Listing listing = dialog.getListing();
            Book book = listing.getBook();
            User owner = listing.getOwner();

            Optional<Message> lastMsg = messageRepo.findTop1ByDialogIdOrderByCreatedAtDesc(dialog.getId());
            String lastContent = lastMsg.map(Message::getContent).orElse(null);
            String lastAuthor = lastMsg.map(m -> m.getAuthor().getName()).orElse(null);
            LocalDateTime lastTime = lastMsg.map(Message::getCreatedAt).orElse(null);

            String imgUrl = listingImageRepo.findByListingId(listing.getId()).stream()
                    .map(ListingImage::getUrl).findFirst().orElse(null);

            return new DialogDto(
                    dialog.getId(), listing.getId(), book.getTitle(), book.getAuthor(), imgUrl,
                    listing.getCondition().name(), owner.getId(), owner.getName(), owner.getAvatarUrl(),
                    lastContent, lastAuthor, lastTime
            );
        }).toList();
    }

    public DialogDto getDialogDetails(Long dialogId, Long userId) {
        Dialog dialog = dialogRepo.findById(dialogId)
                .orElseThrow(() -> new NotFoundException("Dialog not found"));

        if (!isParticipant(dialog, userId)) throw new ForbiddenException("Access denied");

        Listing listing = dialog.getListing();
        Book book = listing.getBook();
        User owner = listing.getOwner();

        Optional<Message> lastMsg = messageRepo.findTop1ByDialogIdOrderByCreatedAtDesc(dialog.getId());
        String lastContent = lastMsg.map(Message::getContent).orElse(null);
        String lastAuthor = lastMsg.map(m -> m.getAuthor().getName()).orElse(null);
        LocalDateTime lastTime = lastMsg.map(Message::getCreatedAt).orElse(null);

        String imgUrl = listingImageRepo.findByListingId(listing.getId()).stream()
                .map(ListingImage::getUrl).findFirst().orElse(null);

        return new DialogDto(
                dialog.getId(), listing.getId(), book.getTitle(), book.getAuthor(), imgUrl,
                listing.getCondition().name(), owner.getId(), owner.getName(), owner.getAvatarUrl(),
                lastContent, lastAuthor, lastTime
        );
    }

    public List<MessageDto> getMessages(Long dialogId, Long userId, int page, int size) {
        Dialog dialog = dialogRepo.findById(dialogId)
                .orElseThrow(() -> new NotFoundException("Dialog not found"));

        if (!isParticipant(dialog, userId)) throw new ForbiddenException("Access denied");

        Page<Message> messages = messageRepo.findByDialogIdOrderByCreatedAtDesc(dialogId, PageRequest.of(page, size));
        return messages.stream().map(msg -> new MessageDto(
                msg.getId(), msg.getAuthor().getId(), msg.getAuthor().getName(),
                msg.getContent(),
                chatImageRepo.findByMessageId(msg.getId()).stream().map(ChatImage::getUrl).toList(),
                msg.getExchange() != null ? exchangeUseCase.toDto(msg.getExchange()) : null,
                msg.isExchangeProposal(),
                msg.getCreatedAt()
        )).toList();
    }

    @Transactional
    public void sendMessage(
            Long userId,
            Long listingId,
            String content,
            List<MultipartFile> images
    ) {
        // 1) Подтягиваем sender и listing
        User sender  = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Listing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        // 2) Ищем существующий диалог
        Dialog dialog = dialogRepo
                .findByListingIdAndUserId(listingId, userId)
                .orElseGet(() -> dialogRepo.save(
                        Dialog.builder()
                                .listing(listing)
                                .owner(listing.getOwner())
                                .otherParticipant(sender)
                                .build()
                ));

        // 3) Проверяем, что пользователь — участник
        if (!userId.equals(dialog.getOwner().getId()) &&
                !userId.equals(dialog.getOtherParticipant().getId())) {
            throw new ForbiddenException("Вы не участник диалога");
        }

        // 4) Сохраняем само сообщение
        Message message = Message.builder()
                .dialog(dialog)
                .author(sender)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build();
        message = messageRepo.save(message);

        // 5) Сохраняем картинки (ваш код)
        if (images != null) {
            for (MultipartFile file : images) {
                String url = imageService.saveImage("messages", message.getId(), file);
                chatImageRepo.save(
                        ChatImage.builder()
                                .message(message)
                                .url(url)
                                .build()
                );
            }
        }
    }


    @Transactional
    public void sendExchangeProposal(Long userId, Long listingId, Long offeredListingId) {
        // 1) Загружаем сущности
        Listing selected = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));
        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        SecurityUtil.assertNotBanned(sender);

        User owner = selected.getOwner();

        // 2) Ищем существующий диалог по listingId и userId
        Dialog dialog = dialogRepo
                .findByListingIdAndUserId(listingId, userId)
                .orElseGet(() -> dialogRepo.save(
                        Dialog.builder()
                                .listing(selected)
                                .owner(owner)
                                .otherParticipant(sender)
                                .build()
                ));

        // 3) Проверяем, что текущий юзер действительно участник
        Long oId = dialog.getOwner().getId();
        Long pId = dialog.getOtherParticipant().getId();
        if (!userId.equals(oId) && !userId.equals(pId)) {
            throw new ForbiddenException("You are not a participant of this dialog");
        }

        // 4) Создаём сущность обмена
        Exchange exchange = exchangeUseCase.proposeExchangeEntity(
                userId,
                new ExchangeCreateDto(offeredListingId, listingId)
        );

        // 5) Сохраняем сообщение с предложением обмена
        Message msg = Message.builder()
                .dialog(dialog)
                .author(sender)
                .content("Предложение обмена")
                .exchange(exchange)
                .isExchangeProposal(true)
                .createdAt(LocalDateTime.now())
                .build();
        messageRepo.save(msg);
    }


//    public Long createDialogOnly(Long userId, Long listingId) {
//        Listing listing = listingRepo.findById(listingId)
//                .orElseThrow(() -> new NotFoundException("Listing not found"));
//        User sender = userRepo.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//        SecurityUtil.assertNotBanned(sender);
//
//        User owner = listing.getOwner();
//
//        return dialogRepo.findDialogAnyDirection(userId, owner.getId(), listingId)
//                .map(Dialog::getId)
//                .orElseGet(() -> dialogRepo.save(Dialog.builder()
//                        .owner(owner)
//                        .otherParticipant(sender)
//                        .listing(listing)
//                        .build()).getId());
//    }

    private boolean isParticipant(Dialog dialog, Long userId) {
        return dialog.getOwner().getId().equals(userId) || dialog.getOtherParticipant().getId().equals(userId);
    }
}

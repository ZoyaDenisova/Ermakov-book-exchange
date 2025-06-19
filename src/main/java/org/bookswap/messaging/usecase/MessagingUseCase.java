package org.bookswap.messaging.usecase;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.SecurityUtil;
import org.bookswap.catalog.entity.Book;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ForbiddenException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.exchange.dto.ExchangeCreateDto;
import org.bookswap.exchange.dto.ExchangeDto;
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

    @Transactional(readOnly = true)
    public List<DialogDto> getUserDialogs(Long userId) {
        List<Dialog> dialogs = dialogRepo.findUserDialogsOrderedByLastMessage(userId);
        return dialogs.stream().map(dialog -> {
            Listing listing = dialog.getListing();
            Book book = listing.getBook();
            User owner = listing.getOwner();

            Optional<Message> lastMsg = messageRepo.findTop1ByDialogIdOrderByCreatedAtDesc(dialog.getId());
            String lastContent = lastMsg.map(Message::getContent).orElse(null);
            String lastAuthor = lastMsg.map(msg -> msg.getAuthor().getName()).orElse(null);
            LocalDateTime lastTime = lastMsg.map(Message::getCreatedAt).orElse(null);

            String imgUrl = listingImageRepo.findByListingId(listing.getId()).stream()
                    .map(ListingImage::getUrl).findFirst().orElse(null);

            return new DialogDto(
                    dialog.getId(),
                    listing.getId(),
                    book.getTitle(),
                    book.getAuthor(),
                    imgUrl,
                    listing.getCondition().name(),
                    owner.getId(),
                    owner.getName(),
                    owner.getAvatarUrl(),
                    lastContent,
                    lastAuthor,
                    lastTime
            );
        }).toList();
    }

    @Transactional(readOnly = true)
    public DialogDto getDialogDetails(Long dialogId, Long userId) {
        Dialog dialog = dialogRepo.findById(dialogId)
                .orElseThrow(() -> new NotFoundException("Dialog not found"));

        if (!dialog.getUser1().getId().equals(userId) && !dialog.getUser2().getId().equals(userId))
            throw new ForbiddenException("Access denied");

        Listing listing = dialog.getListing();
        Book book = listing.getBook();
        User owner = listing.getOwner();

        Optional<Message> lastMsg = messageRepo.findTop1ByDialogIdOrderByCreatedAtDesc(dialog.getId());
        String lastContent = lastMsg.map(Message::getContent).orElse(null);
        String lastAuthor = lastMsg.map(msg -> msg.getAuthor().getName()).orElse(null);
        LocalDateTime lastTime = lastMsg.map(Message::getCreatedAt).orElse(null);

        String imgUrl = listingImageRepo.findByListingId(listing.getId()).stream()
                .map(ListingImage::getUrl).findFirst().orElse(null);

        return new DialogDto(
                dialog.getId(),
                listing.getId(),
                book.getTitle(),
                book.getAuthor(),
                imgUrl,
                listing.getCondition().name(),
                owner.getId(),
                owner.getName(),
                owner.getAvatarUrl(),
                lastContent,
                lastAuthor,
                lastTime
        );
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(Long dialogId, Long userId, int page, int size) {
        Dialog dialog = dialogRepo.findById(dialogId)
                .orElseThrow(() -> new NotFoundException("Dialog not found"));

        if (!dialog.getUser1().getId().equals(userId) && !dialog.getUser2().getId().equals(userId))
            throw new ForbiddenException("Access denied");

        Page<Message> messages = messageRepo.findByDialogIdOrderByCreatedAtDesc(dialogId, PageRequest.of(page, size));

        return messages.stream().map(msg -> {
            List<String> images = chatImageRepo.findByMessageId(msg.getId()).stream()
                    .map(ChatImage::getUrl).toList();

            ExchangeDto exchangeDto = msg.getExchange() != null
                    ? exchangeUseCase.toDto(msg.getExchange())
                    : null;

            return new MessageDto(
                    msg.getId(),
                    msg.getAuthor().getId(),
                    msg.getAuthor().getName(),
                    msg.getContent(),
                    images,
                    exchangeDto,
                    msg.isExchangeProposal(),
                    msg.getCreatedAt()
            );
        }).toList();
    }

    @Transactional
    public void sendMessage(Long userId, Long listingId, String content, List<MultipartFile> images) {
        Listing listing = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        SecurityUtil.assertNotBanned(sender);

        User receiver = listing.getOwner();
        if (receiver.getId().equals(userId)) {
            throw new BadRequestException("Cannot send message to your own listing");
        }

        Dialog dialog = getOrCreateDialog(userId, receiver.getId(), listing);

        if (images != null && images.size() > 3)
            throw new BadRequestException("Maximum 3 images allowed");

        Message msg = messageRepo.save(Message.builder()
                .dialog(dialog)
                .author(sender)
                .content(content)
                .createdAt(LocalDateTime.now())
                .build());

        if (images != null) {
            for (MultipartFile file : images) {
                String url = imageService.saveImage("messages", msg.getId(), file);
                chatImageRepo.save(ChatImage.builder().message(msg).url(url).build());
            }
        }
    }

    @Transactional
    public void sendExchangeProposal(Long userId, Long listingId, Long offeredListingId) {
        Listing selected = listingRepo.findById(listingId)
                .orElseThrow(() -> new NotFoundException("Listing not found"));

        User sender = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        SecurityUtil.assertNotBanned(sender);

        User receiver = selected.getOwner();
        if (receiver.getId().equals(userId)) {
            throw new BadRequestException("Cannot propose exchange to your own listing");
        }

        Dialog dialog = getOrCreateDialog(userId, receiver.getId(), selected);

        ExchangeCreateDto dto = new ExchangeCreateDto(offeredListingId, selected.getId());
        Exchange exchange = exchangeUseCase.proposeExchangeEntity(userId, dto);

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

    private Dialog getOrCreateDialog(Long user1Id, Long user2Id, Listing listing) {
        Long lowId = Math.min(user1Id, user2Id);
        Long highId = Math.max(user1Id, user2Id);

        return dialogRepo.findBetweenUsersForListing(lowId, highId, listing.getId())
                .orElseGet(() -> {
                    Dialog d = Dialog.builder()
                            .user1(userRepo.findById(lowId).orElseThrow(() -> new NotFoundException("User1 not found")))
                            .user2(userRepo.findById(highId).orElseThrow(() -> new NotFoundException("User2 not found")))
                            .listing(listing)
                            .build();
                    return dialogRepo.save(d);
                });
    }

}
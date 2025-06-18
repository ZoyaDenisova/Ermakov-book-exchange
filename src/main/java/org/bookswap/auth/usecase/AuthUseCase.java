package org.bookswap.auth.usecase;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.*;
import org.bookswap.auth.entity.Role;
import org.bookswap.auth.entity.Session;
import org.bookswap.auth.entity.User;
import org.bookswap.auth.repository.SessionRepo;
import org.bookswap.auth.repository.UserRepo;
import org.bookswap.auth.security.PasswordHasher;
import org.bookswap.auth.security.TokenManager;
import org.bookswap.catalog.entity.Book;
import org.bookswap.catalog.entity.WantedBook;
import org.bookswap.catalog.repository.WantedBookRepo;
import org.bookswap.common.exception.BadRequestException;
import org.bookswap.common.exception.ConflictException;
import org.bookswap.common.exception.NotFoundException;
import org.bookswap.common.exception.UnauthorizedException;
import org.bookswap.listings.entity.City;
import org.bookswap.listings.entity.Listing;
import org.bookswap.listings.repository.CityRepo;
import org.bookswap.listings.repository.ListingRepo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static org.bookswap.auth.security.SecurityUtil.assertNotBanned;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthUseCase {

    private final UserRepo userRepo;
    private final ListingRepo listingRepo;
    private final WantedBookRepo wantedBookRepo;
    private final SessionRepo sessionRepo;
    private final CityRepo cityRepo;
    private final PasswordHasher hasher;
    private final TokenManager tokenManager;

    public TokenPairDto register(RegisterDto dto) {
        userRepo.findByEmail(dto.email()).ifPresent(u -> {
            throw new ConflictException("Email already in use");
        });

        City city = cityRepo.findById(dto.cityId())
                .orElseThrow(() -> new BadRequestException("City not found"));

        User user = User.builder()
                .name(dto.name())
                .email(dto.email())
                .passwordHash(hasher.hash(dto.password()))
                .city(city)
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .isBanned(false)
                .build();

        User saved = userRepo.save(user);
        return issueTokens(saved);
    }

    public TokenPairDto login(LoginDto dto) {
        User user = userRepo.findByEmail(dto.email())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!hasher.verify(user.getPasswordHash(), dto.password())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        assertNotBanned(user);

        return issueTokens(user);
    }

    public void updateUser(Long userId, UpdateUserDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        assertNotBanned(user);

        if (dto.name() != null) user.setName(dto.name());
        if (dto.avatarUrl() != null) user.setAvatarUrl(dto.avatarUrl());
        if (dto.cityId() != null) {
            City city = cityRepo.findById(dto.cityId())
                    .orElseThrow(() -> new BadRequestException("City not found"));
            user.setCity(city);
        }

        userRepo.save(user);
    }

    public void changePassword(Long userId, ChangePasswordDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        assertNotBanned(user);

        if (!hasher.verify(user.getPasswordHash(), dto.oldPassword())) {
            throw new UnauthorizedException("Old password is incorrect");
        }

        user.setPasswordHash(hasher.hash(dto.newPassword()));
        userRepo.save(user);
    }

    public void changeRole(Long userId, ChangeRoleDto dto) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        assertNotBanned(user);

        try {
            user.setRole(Role.valueOf(dto.role()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + dto.role());
        }
        userRepo.save(user);
    }

    public UserPublicDto getPublicProfile(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        List<Listing> listings = listingRepo.findByOwnerIdAndIsOpenTrueAndIsBlockedFalse(userId);
        List<WantedBook> wants = wantedBookRepo.findByUserId(userId);

        List<BookShortDto> canOffer = listings.stream()
                .map(l -> {
                    Book b = l.getBook();
                    return new BookShortDto(b.getId(), b.getTitle(), b.getAuthor());
                })
                .distinct()
                .toList();

        List<BookShortDto> wantList = wants.stream()
                .map(w -> {
                    Book b = w.getBook();
                    return new BookShortDto(b.getId(), b.getTitle(), b.getAuthor());
                })
                .distinct()
                .toList();

        return new UserPublicDto(
                user.getId(),
                user.getName(),
                user.getAvatarUrl(),
                user.getCity() != null ? user.getCity().getName() : null,
                canOffer,
                wantList
        );
    }

    public UserDto getById(Long id) {
        User u = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));

        return toDto(u);
    }

    public List<UserDto> getAll() {
        return userRepo.findAll().stream().map(this::toDto).toList();
    }

    public void banUser(Long targetId, Long actorId) {
        User actor = userRepo.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Invoker not found"));

        assertNotBanned(actor);

        if (!userRepo.existsById(targetId)) {
            throw new NotFoundException("Target user not found");
        }

        userRepo.banUser(targetId);
    }


    public void unbanUser(Long targetId, Long actorId) {
        User actor = userRepo.findById(actorId)
                .orElseThrow(() -> new NotFoundException("Invoker not found"));

        assertNotBanned(actor);

        if (!userRepo.existsById(targetId)) {
            throw new NotFoundException("Target user not found");
        }

        userRepo.unbanUser(targetId);
    }

    public TokenPairDto refreshToken(String refreshToken) {
        Session session = sessionRepo.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid token"));

        if (session.getExpiresAt().isBefore(LocalDateTime.now())) {
            sessionRepo.deleteByRefreshToken(refreshToken);
            throw new UnauthorizedException("Token expired");
        }

        return issueTokens(session.getUser());
    }

    public List<SessionDto> getUserSessions(Long userId) {
        return sessionRepo.findByUserId(userId).stream()
                .map(s -> new SessionDto(
                        s.getId(),
                        s.getRefreshToken(),
                        s.getCreatedAt().toString(),
                        s.getExpiresAt().toString()))
                .toList();
    }

    public void logout(String refreshToken) {
        sessionRepo.deleteByRefreshToken(refreshToken);
    }

    public void logoutAll(Long userId) {
        sessionRepo.deleteByUserId(userId);
    }

    private TokenPairDto issueTokens(User user) {
        TokenPairDto tokens = tokenManager.generate(user.getId(), user.getRole().name());

        Session session = Session.builder()
                .user(user)
                .refreshToken(tokens.refreshToken())
                .createdAt(LocalDateTime.now())
                .expiresAt(tokens.refreshExpires())
                .build();

        sessionRepo.save(session);
        return tokens;
    }

    private UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getRole().name(),
                u.isBanned(),
                u.getCity() != null ? u.getCity().getId() : null
        );
    }
}
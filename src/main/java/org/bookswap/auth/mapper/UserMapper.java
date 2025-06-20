package org.bookswap.auth.mapper;

import lombok.RequiredArgsConstructor;
import org.bookswap.auth.dto.UserDto;
import org.bookswap.auth.entity.User;
import org.bookswap.listings.mapper.CityMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final CityMapper cityMapper;

    public UserDto toDto(User u) {
        return new UserDto(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getAvatarUrl(),
                u.getRole().name(),
                u.isBanned(),
                u.getCity() != null ? cityMapper.toDto(u.getCity()) : null
        );
    }
}
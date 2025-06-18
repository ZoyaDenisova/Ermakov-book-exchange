package org.bookswap.auth.security;

import org.bookswap.auth.entity.Role;
import org.bookswap.auth.entity.User;
import org.bookswap.common.exception.ForbiddenException;

public class SecurityUtil {

    public static void assertHasRole(String actualRole, Role... allowedRoles) {
        for (Role role : allowedRoles) {
            if (role.name().equals(actualRole)) return;
        }
        throw new ForbiddenException("Access denied: insufficient role");
    }

    public static void assertIsSelfOrThrow(Long currentUserId, Long targetUserId) {
        if (!currentUserId.equals(targetUserId)) {
            throw new ForbiddenException("Access denied: not the resource owner");
        }
    }

    public static void assertNotBanned(User user) {
        if (user.isBanned()) {
            throw new ForbiddenException("User is banned");
        }
    }


}

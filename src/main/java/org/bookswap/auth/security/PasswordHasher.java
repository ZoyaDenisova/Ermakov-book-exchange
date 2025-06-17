package org.bookswap.auth.security;

public interface PasswordHasher {
    String hash(String password);
    boolean verify(String hash, String rawPassword);
}

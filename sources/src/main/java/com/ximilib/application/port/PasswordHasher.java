package com.ximilib.application.port;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String hashedPassword);

    String encrypt(String plainText);
}

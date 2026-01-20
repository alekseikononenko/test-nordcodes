package com.autoqa.utils;

import java.security.SecureRandom;

public class TestDataGenerator {

    private static final String CHAR_POOL = "ABCDEF0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    // Генерация случайного токена длиной 32 символа
    public static String generateToken() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 32; i++) {
            sb.append(CHAR_POOL.charAt(RANDOM.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        String token = generateToken();
        System.out.println(token);
    }
}

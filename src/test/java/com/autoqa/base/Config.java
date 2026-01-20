package com.autoqa.base;

public class Config {
    // URL тестируемого приложения
    public static final String BASE_URL = "http://localhost:8080/endpoint";

    // Статический API-ключ
    public static final String API_KEY = "qazWSXedc";

    // WireMock URL (для моков внешнего сервиса)
    public static final String MOCK_URL = "http://localhost:8888";

    // Длина токена
    public static final int TOKEN_LENGTH = 32;

    //Конкретный токен
    public static final String Token1 = "A823456789012345678901234567890B";
}

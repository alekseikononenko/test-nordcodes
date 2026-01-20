package com.autoqa.base;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

public class BaseTest {

    protected static WireMockServer wireMockServer;

    @BeforeAll
    public static void setupWireMock() {
        wireMockServer = new WireMockServer(options().port(8888));
        wireMockServer.start();
        // Проверка, что сервер запущен
        if (!wireMockServer.isRunning()) {
            throw new IllegalStateException("WireMock не запустился на 8888!");
        }
        System.out.println("WireMock успешно запущен на порту 8888");
    }

    @AfterAll
    public static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
            System.out.println("WireMock остановлен");
        }
    }
}

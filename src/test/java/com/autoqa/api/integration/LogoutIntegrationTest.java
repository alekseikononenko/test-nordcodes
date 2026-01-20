package com.autoqa.api.integration;

import com.autoqa.base.BaseTest;
import com.autoqa.base.Config;
import com.autoqa.utils.ApiClient;
import com.autoqa.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("API")
@Feature("LOGOUT")
@Story("Интеграционные проверки выхода из системы")
@DisplayName("Интеграционные проверки выхода из системы (LOGOUT)")
public class LogoutIntegrationTest extends BaseTest {

    private String lastUsedToken;

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();

        // По умолчанию внешний /auth доступен и отвечает OK
        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));
    }

    @AfterEach
    public void cleanupAfterTest() {
        if (lastUsedToken != null) {
            ApiClient.sendPost(lastUsedToken, "LOGOUT", Config.API_KEY);
            lastUsedToken = null;
        }
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGOUT после успешного LOGIN должен вернуть 200 OK")
    @Description("LOGOUT после успешного LOGIN. Ожидаем 200 OK.")
    @Step("LOGOUT после LOGIN")
    public void logoutAfterLogin_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response loginResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Проверка успешного LOGIN", () -> {
            Allure.addAttachment("LOGIN response", loginResponse.getBody().asString());
            assertEquals(200, loginResponse.getStatusCode(), "Ожидается код 200 для успешного LOGIN перед LOGOUT");
        });

        // LOGOUT
        Response logoutResponse = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("Проверка успешного LOGOUT", () -> {
            Allure.addAttachment("LOGOUT response", logoutResponse.getBody().asString());
            assertEquals(200, logoutResponse.getStatusCode(), "Ожидается код 200 для успешного LOGOUT");
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGIN после LOGOUT с тем же токеном должен вернуть 200 OK")
    @Description("LOGIN → LOGOUT → LOGIN с тем же токеном. Ожидаем повторный LOGIN = OK.")
    @Step("LOGIN после LOGOUT")
    public void loginAfterLogout_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response firstLogin = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        Allure.addAttachment("First LOGIN", firstLogin.getBody().asString());
        assertEquals(200, firstLogin.getStatusCode(), "Ожидается код 200 для первого LOGIN");

        // LOGOUT
        Response logout = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        Allure.addAttachment("LOGOUT", logout.getBody().asString());
        assertEquals(200, logout.getStatusCode(), "Ожидается код 200 для LOGOUT");

        // Повторный LOGIN
        Response secondLogin = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Проверка повторного LOGIN после LOGOUT", () -> {
            Allure.addAttachment("Second LOGIN", secondLogin.getBody().asString());
            assertEquals(200, secondLogin.getStatusCode(), "Ожидается код 200 для повторного LOGIN после LOGOUT");
            assertEquals("OK", secondLogin.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGOUT без предварительного LOGIN должен вернуть 200 OK")
    @Description("LOGOUT без предварительного LOGIN. Ожидаем 200 OK.")
    @Step("LOGOUT без LOGIN")
    public void logoutWithoutLogin_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        Response response = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("Проверка ответа LOGOUT без LOGIN", () -> {
            Allure.addAttachment("LOGOUT response", response.getBody().asString());
            assertEquals(200, response.getStatusCode(), "Ожидается код 200 при LOGOUT без предварительного LOGIN");
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Повторный LOGOUT должен вернуть тот же результат")
    @Description("Повторный LOGOUT одного и того же токена. Ожидаем тот же результат.")
    @Step("Повторный LOGOUT")
    public void repeatedLogout_shouldReturnSameResult() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response login = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        assertEquals(200, login.getStatusCode(), "Ожидается код 200 для LOGIN перед LOGOUT");

        // Первый LOGOUT
        Response firstLogout = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        Allure.addAttachment("First LOGOUT", firstLogout.getBody().asString());
        assertEquals(200, firstLogout.getStatusCode(), "Ожидается код 200 для первого LOGOUT");

        // Повторный LOGOUT
        Response secondLogout = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        Allure.addAttachment("Second LOGOUT", secondLogout.getBody().asString());
        assertEquals(200, secondLogout.getStatusCode(), "Ожидается код 200 для повторного LOGOUT");
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGOUT при недоступности внешнего /auth сервиса должен вернуть 500")
    @Description("LOGOUT при недоступности внешнего /auth сервиса.")
    @Step("LOGOUT при ошибке внешнего сервиса")
    public void logoutWhenAuthServiceUnavailable_shouldReturnServerError() {
        String token = TestDataGenerator.generateToken();

        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("{\"result\":\"ERROR\"}")));

        Response response = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("Проверка серверной ошибки", () -> {
            Allure.addAttachment("LOGOUT response", response.getBody().asString());
            assertEquals(500, response.getStatusCode(), "Ожидается код 500 при недоступности внешнего /auth сервиса");
        });
    }
}
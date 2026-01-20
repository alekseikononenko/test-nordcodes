package com.autoqa.api.integration;

import com.autoqa.base.BaseTest;
import com.autoqa.base.Config;
import com.autoqa.utils.ApiClient;
import com.autoqa.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("API")
@Feature("LOGIN")
@Story("Интеграционные проверки входных данных")
@DisplayName("Интеграционные проверки входа в систему (LOGIN)")
public class LoginIntegrationTest extends BaseTest {

    private String lastUsedToken;

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();
        // Заглушка для /auth — любые токены валидны по умолчанию
        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));
    }

    @AfterEach
    public void logoutAfterTest() {
        if (lastUsedToken != null) {
            Response logoutResponse = ApiClient.sendPost(lastUsedToken, "LOGOUT", Config.API_KEY);
            Allure.step("LOGOUT после теста", () -> {
                int status = logoutResponse.getStatusCode();
                String body = logoutResponse.getBody().asString();
                Allure.addAttachment("HTTP Response Body LOGOUT", body);
            });
            lastUsedToken = null;
        }
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGIN с валидным токеном должен быть успешным")
    @Description("Успешный LOGIN с валидным токеном. Ожидаем 200 OK и result=OK")
    @Step("LOGIN с валидным токеном")
    public void loginWithValidToken_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGIN", body);
            assertEquals(200, status);
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("Повторный LOGIN с тем же токеном должен вернуть 409 или ERROR")
    @Description("Повторный LOGIN тем же токеном. Ожидаем 409 или ERROR")
    @Step("Повторный LOGIN с тем же токеном")
    public void repeatedLoginWithSameToken_shouldReturnConflict() {
        String token = TestDataGenerator.generateToken();

        // Первый LOGIN
        Response firstResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Первый LOGIN - проверка успешного ответа", () -> {
            int status = firstResponse.getStatusCode();
            String body = firstResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body First LOGIN", body);
            assertEquals(200, status);
            assertEquals("OK", firstResponse.jsonPath().getString("result"));
        });

        // Второй LOGIN
        Response secondResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        Allure.step("Второй LOGIN - проверка кода 409 или ERROR", () -> {
            int status = secondResponse.getStatusCode();
            String body = secondResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body Second LOGIN", body);
            assertEquals(409, status, "Ожидается 409 для повторного LOGIN или 400 с result=ERROR");
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGIN после LOGOUT должен быть успешным")
    @Description("LOGIN после LOGOUT. Ожидаем успешный LOGIN после выхода.")
    @Step("LOGIN после LOGOUT")
    public void loginAfterLogout_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response loginResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        Allure.step("Проверка успешного LOGIN", () -> {
            int status = loginResponse.getStatusCode();
            String body = loginResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGIN", body);
            assertEquals(200, status);
            assertEquals("OK", loginResponse.jsonPath().getString("result"));
        });

        // LOGOUT
        Response logoutResponse = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        Allure.step("Проверка успешного LOGOUT", () -> {
            int status = logoutResponse.getStatusCode();
            String body = logoutResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGOUT", body);
            assertEquals(200, status);
        });

        // Повторный LOGIN после LOGOUT
        Response secondLoginResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Проверка успешного LOGIN после LOGOUT", () -> {
            int status = secondLoginResponse.getStatusCode();
            String body = secondLoginResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body Second LOGIN", body);
            assertEquals(200, status);
            assertEquals("OK", secondLoginResponse.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с неверным API-ключом должен вернуть 401 или 403")
    @Description("LOGIN с неверным API-ключом. Ожидаем 403 или 401.")
    @Step("LOGIN с неверным API-ключом")
    public void loginWithInvalidApiKey_shouldReturnForbidden() {
        String token = TestDataGenerator.generateToken();
        String invalidApiKey = "INVALID_API_KEY";

        Response response = ApiClient.sendPost(token, "LOGIN", invalidApiKey);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(status == 401 || status == 403, true, "Ожидается 401 или 403 при неверном API-ключе");
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN без API-ключа должен вернуть 401 или 403")
    @Description("LOGIN без API-ключа. Ожидаем 403 или 401.")
    @Step("LOGIN без API-ключа")
    public void loginWithoutApiKey_shouldReturnForbidden() {
        String token = TestDataGenerator.generateToken();

        Response response = ApiClient.sendPost(token, "LOGIN", "");

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(status == 401 || status == 403, true, "Ожидается 401 или 403 при отсутствии API-ключа");
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN при недоступности /auth должен вернуть 500 или 504")
    @Description("LOGIN при недоступности /auth (500 Internal Server Error или таймаут).")
    @Step("LOGIN при недоступности /auth")
    public void loginWhenAuthServiceUnavailable_shouldReturnServerError() {
        String token = TestDataGenerator.generateToken();

        // Перекрываем stub /auth, чтобы возвращать 500
        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"ERROR\"}")));

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(status == 500 || status == 504, true, "Ожидается 500 или 504 при недоступности /auth");
        });
    }
}
package com.autoqa.api.validation;

import com.autoqa.base.BaseTest;
import com.autoqa.base.Config;
import com.autoqa.utils.ApiClient;
import com.autoqa.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Epic("API")
@Feature("LOGIN")
@Story("Валидация входных данных")
@DisplayName("Валидация входа в систему (LOGIN)")
public class LoginValidationTest extends BaseTest {

    private String lastUsedToken;

    @BeforeAll
    public static void logoutAllTestTokens() {
        String[] testTokens = new String[] {
            "A193456789012345678901234567890B",
            "SHORTTOKEN123",
            "A823456789012345678901234567890BEXTRA",
            "INVALID!@#TOKEN123456789012345",
            "A123456789012345678901234567890Z",
            "11234567890123456789012345678901",
            "A1B2C3D4E5F6G7H8J9K0L1M2N3O4P5",
            "12345678901234567890123456789012"
        };

        for (String token : testTokens) {
            try {
                Response response = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
                System.out.println("LOGOUT token=" + token + " -> " + response.getStatusCode());
            } catch (Exception e) {
                System.out.println("Ошибка при LOGOUT token=" + token + ": " + e.getMessage());
            }
        }
    }

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();  // сброс всех stub’ов и записей
        // Создаём stub для /auth, чтобы позитивные токены всегда принимались
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
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

            lastUsedToken = null; // очищаем токен
        }
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("LOGIN с валидным токеном должен быть успешным")
    @Description("""
        Позитивный кейс.
        Валидный токен (реальный, принимается приложением).
        Ожидаем успешный LOGIN.
        """)
    @Step("LOGIN с валидным токеном")
    public void loginWithValidToken_shouldReturnOk() {
        String token = "A193456789012345678901234567890B"; // реально принимается приложением

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(200, status, "Ожидается код 200 для успешного LOGIN с валидным токеном");
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с коротким токеном должен вернуть 400")
    @Description("""
        Негативный кейс.
        Длина токена меньше ожидаемой(<32).
        Ожидаем ошибку валидации 400.
        """)
    @Step("LOGIN с коротким токеном")
    public void loginWithShortToken_shouldReturn400() {
        String token = "SHORTTOKEN123";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            if (status != 400) {
                Allure.addAttachment("HTTP Response Body", body);
            }
            assertEquals(400, status, "Ожидается код 400 при ошибке валидации токена");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с длинным токеном должен вернуть 400")
    @Description("""
        Негативный кейс.
        Длина токена больше ожидаемой(>32).
        Ожидаем ошибку валидации 400.
        """)
    @Step("LOGIN с длинным токеном")
    public void loginWithLongToken_shouldReturn400() {
        String token = "A823456789012345678901234567890BEXTRA";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            if (status != 400) {
                Allure.addAttachment("HTTP Response Body", body);
            }
            assertEquals(400, status, "Ожидается код 400 при ошибке валидации токена");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с токеном с недопустимыми символами должен вернуть 400")
    @Description("""
        Негативный кейс.
        Токен содержит недопустимые символы(не буквы и не цифры).
        Ожидаем ошибку валидации 400.
        """)
    @Step("LOGIN с токеном с недопустимыми символами")
    public void loginWithInvalidCharacters_shouldReturn400() {
        String token = "INVALID!@#TOKEN123456789012345";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            if (status != 400) {
                Allure.addAttachment("HTTP Response Body", body);
            }
            assertEquals(400, status, "Ожидается код 400 при ошибке валидации токена");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с токеном: буква в начале и в конце должен быть успешным")
    @Description("""
        Позитивный граничный кейс.
        Токен корректной длины, начинается и заканчивается буквой(A‐Z0‐9).
        Ожидаем успешный LOGIN.
        """)
    @Step("LOGIN с токеном: буква в начале и в конце")
    public void loginTokenStartsAndEndsWithLetters_shouldReturnOk() {
        String token = "A123456789012345678901234567890Z";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(200, status, "Ожидается код 200 для успешного LOGIN с токеном корректной структуры");
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с токеном: цифра в начале и в конце должен быть успешным")
    @Description("""
        Позитивный граничный кейс.
        Токен корректной длины, начинается и заканчивается цифрой(A‐Z0‐9).
        Ожидаем успешный LOGIN.
        """)
    @Step("LOGIN с токеном: цифра в начале и в конце")
    public void loginTokenStartsAndEndsWithDigits_shouldReturnOk() {
        String token = "11234567890123456789012345678901";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(200, status, "Ожидается код 200 для успешного LOGIN с токеном корректной структуры");
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с токеном с чередованием букв и цифр должен быть успешным")
    @Description("""
        Позитивный граничный кейс(A‐Z0‐9).
        Токен корректной длины.
        Содержит чередование букв и цифр.
        Ожидаем успешный LOGIN.
        """)
    @Step("LOGIN с токеном с чередованием букв и цифр")
    public void loginTokenWithAlternatingLettersAndDigits_shouldReturnOk() {
        String token = "A1B2C3D4E5F6G7H8J9K0L1M2N3O4P5";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(200, status, "Ожидается код 200 для успешного LOGIN с токеном корректной структуры");
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGIN с токеном, состоящим только из цифр, должен быть успешным")
    @Description("""
        Позитивный граничный кейс(A‐Z0‐9).
        Токен корректной длины.
        Состоит только из цифр.
        Ожидаем успешный LOGIN.
        """)
    @Step("LOGIN с токеном, состоящим только из цифр")
    public void loginTokenWithOnlyDigits_shouldReturnOk() {
        String token = "12345678901234567890123456789012";

        Response response = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        lastUsedToken = token;

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);
            assertEquals(200, status, "Ожидается код 200 для успешного LOGIN с токеном корректной структуры");
            assertEquals("OK", response.jsonPath().getString("result"));
        });
    }
}
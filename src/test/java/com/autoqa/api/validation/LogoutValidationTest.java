package com.autoqa.api.validation;

import com.autoqa.base.BaseTest;
import com.autoqa.base.Config;
import com.autoqa.utils.ApiClient;
import com.autoqa.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Epic("API")
@Feature("LOGOUT")
@Story("Валидация выхода из системы")
@DisplayName("Валидация выхода из системы (LOGOUT)")
public class LogoutValidationTest extends BaseTest {

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();
        // Заглушка для /auth — любые токены валидны для позитивных LOGOUT
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGOUT без предварительного LOGIN должен вернуть 200 OK")
    @Description("LOGOUT без предварительного LOGIN. Ожидаем 200 OK.")
    @Step("LOGOUT без LOGIN")
    public void logoutWithoutLogin_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        Response response = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGOUT without LOGIN", body);
            assertEquals(200, status, "Ожидается код 200 при LOGOUT без предварительного LOGIN");
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGOUT с недопустимым токеном должен вернуть 400")
    @Description("LOGOUT с недопустимым токеном. Ожидаем 400.")
    @Step("LOGOUT с недопустимым токеном")
    public void logoutWithInvalidToken_shouldReturn400() {
        String token = "INVALID!@#TOKEN123";

        Response response = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGOUT with invalid token", body);
            assertEquals(400, status, "Ожидается код 400 при LOGOUT с недопустимым токеном");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("LOGOUT без токена должен вернуть 400")
    @Description("LOGOUT без токена. Ожидаем 400.")
    @Step("LOGOUT с отсутствующим токеном")
    public void logoutWithMissingToken_shouldReturn400() {
        Response response = ApiClient.sendPost("", "LOGOUT", Config.API_KEY);

        Allure.step("Проверка HTTP-кода и тела ответа", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body LOGOUT with missing token", body);
            assertEquals(400, status, "Ожидается код 400 при LOGOUT без токена");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("Повторный LOGOUT должен вернуть тот же результат")
    @Description("Повторный LOGOUT. Результат должен быть idem (такой же, как предыдущий).")
    @Step("Повторный LOGOUT")
    public void repeatedLogout_shouldReturnSameResult() {
        String token = TestDataGenerator.generateToken();

        // Первый LOGOUT
        Response firstResponse = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        int firstStatus = firstResponse.getStatusCode();
        String firstBody = firstResponse.getBody().asString();

        Allure.addAttachment("HTTP Response Body First LOGOUT", firstBody);

        // Второй LOGOUT
        Response secondResponse = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        int secondStatus = secondResponse.getStatusCode();
        String secondBody = secondResponse.getBody().asString();

        Allure.addAttachment("HTTP Response Body Second LOGOUT", secondBody);

        Allure.step("Проверка, что повторный LOGOUT возвращает тот же результат", () -> {
            assertEquals(firstStatus, secondStatus, "Ожидается, что повторный LOGOUT возвращает тот же код HTTP");
            assertEquals(firstBody, secondBody, "Ожидается, что повторный LOGOUT возвращает такое же тело ответа");
        });
    }
}
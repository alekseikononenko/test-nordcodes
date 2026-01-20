package com.autoqa.api.validation;

import com.autoqa.base.BaseTest;
import com.autoqa.base.Config;
import com.autoqa.utils.ApiClient;
import com.autoqa.utils.TestDataGenerator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Epic("API")
@Feature("ACTION")
@Story("Валидация выполнения действия")
@DisplayName("Валидация выполнения действия (ACTION)")
public class ActionValidationTest extends BaseTest {

    private String lastUsedToken;

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();

        // Внешний auth по умолчанию доступен
        wireMockServer.stubFor(post(urlEqualTo("/auth"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));
    }

    @AfterEach
    public void logoutAfterTest() {
        if (lastUsedToken != null) {
            ApiClient.sendPost(lastUsedToken, "LOGOUT", Config.API_KEY);
            lastUsedToken = null;
        }
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("ACTION без предварительного LOGIN должен вернуть ошибку")
    @Description("""
        ACTION без предварительного LOGIN.
        Ожидаем отказ в выполнении действия.
        """)
    @Step("ACTION без LOGIN")
    public void actionWithoutLogin_shouldReturnError() {
        String token = TestDataGenerator.generateToken();
        lastUsedToken = token;

        Response response = ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ответа ACTION без LOGIN", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);

            assertEquals(400, status, "Ожидается код 400 при попытке ACTION без LOGIN");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @DisplayName("ACTION после LOGOUT должен вернуть ошибку")
    @Description("""
        ACTION после LOGOUT.
        Ожидаем отказ, так как сессия завершена.
        """)
    @Step("ACTION после LOGOUT")
    public void actionAfterLogout_shouldReturnError() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response loginResponse = ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        assertEquals(200, loginResponse.getStatusCode());
        lastUsedToken = token;

        // LOGOUT
        Response logoutResponse = ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);
        assertEquals(200, logoutResponse.getStatusCode());

        // ACTION
        Response actionResponse = ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ACTION после LOGOUT", () -> {
            int status = actionResponse.getStatusCode();
            String body = actionResponse.getBody().asString();
            Allure.addAttachment("HTTP Response Body", body);

            assertEquals(400, status, "Ожидается код 400 при ACTION после LOGOUT");
            assertEquals("ERROR", actionResponse.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("ACTION с невалидным токеном должен вернуть 400")
    @Description("""
        ACTION с невалидным токеном.
        Нарушение формата токена.
        Ожидаем ошибку валидации 400.
        """)
    @Step("ACTION с невалидным токеном")
    public void actionWithInvalidToken_shouldReturn400() {
        String token = "INVALID!@#TOKEN123";

        Response response = ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ошибки валидации токена", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();

            if (status != 400) {
                Allure.addAttachment("HTTP Response Body", body);
            }

            assertEquals(400, status, "Ожидается код 400 при ACTION с невалидным токеном");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @DisplayName("ACTION без токена должен вернуть 400")
    @Description("""
        ACTION без передачи токена.
        Ожидаем ошибку валидации запроса 400.
        """)
    @Step("ACTION без токена")
    public void actionWithMissingToken_shouldReturn400() {
        Response response = ApiClient.sendPost("", "ACTION", Config.API_KEY);

        Allure.step("Проверка ACTION без token", () -> {
            int status = response.getStatusCode();
            String body = response.getBody().asString();

            if (status != 400) {
                Allure.addAttachment("HTTP Response Body", body);
            }

            assertEquals(400, status, "Ожидается код 400 при ACTION без токена");
            assertEquals("ERROR", response.jsonPath().getString("result"));
        });
    }
}

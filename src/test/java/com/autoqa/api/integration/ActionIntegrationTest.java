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
@Feature("ACTION")
@Story("Интеграционные проверки бизнес-логики ACTION")
@DisplayName("Интеграционные проверки выполнения действия (ACTION)")
public class ActionIntegrationTest extends BaseTest {

    private String lastUsedToken;

    @BeforeEach
    public void resetMocks() {
        wireMockServer.resetAll();

        // Заглушка внешнего сервиса /auth
        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));

        // Заглушка внешнего сервиса /doAction
        wireMockServer.stubFor(post("/doAction")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"OK\"}")));
    }

    @AfterEach
    public void logoutAfterTest() {
        if (lastUsedToken != null) {
            Response logoutResponse =
                    ApiClient.sendPost(lastUsedToken, "LOGOUT", Config.API_KEY);

            Allure.step("LOGOUT после теста", () -> {
                Allure.addAttachment(
                        "HTTP Response Body LOGOUT",
                        logoutResponse.getBody().asString()
                );
            });

            lastUsedToken = null;
        }
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @Description("ACTION после успешного LOGIN. Ожидаем 200 OK.")
    @Step("ACTION после LOGIN")
    @DisplayName("ACTION после успешного LOGIN должен вернуть 200 OK")
    public void actionAfterLogin_shouldReturnOk() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        Response loginResponse =
                ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        Allure.step("Проверка успешного LOGIN", () -> {
            Allure.addAttachment(
                    "HTTP Response Body LOGIN",
                    loginResponse.getBody().asString()
            );
            assertEquals(200, loginResponse.getStatusCode());
        });

        // ACTION
        Response actionResponse =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ACTION", () -> {
            Allure.addAttachment(
                    "HTTP Response Body ACTION",
                    actionResponse.getBody().asString()
            );
            assertEquals(200, actionResponse.getStatusCode());
            assertEquals("OK", actionResponse.jsonPath().getString("result"));
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @Description("ACTION без LOGIN. Ожидаем ошибку.")
    @Step("ACTION без LOGIN")
    @DisplayName("ACTION без предварительного LOGIN должен вернуть ошибку")
    public void actionWithoutLogin_shouldReturnError() {
        String token = TestDataGenerator.generateToken();

        Response response =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ошибки", () -> {
            Allure.addAttachment(
                    "HTTP Response Body",
                    response.getBody().asString()
            );
            int status = response.getStatusCode();
            // Ожидается один из кодов 400, 401, 403 при ошибке авторизации
            boolean validStatus = status == 400 || status == 401 || status == 403;
            assertEquals(true, validStatus, "Ожидается код ответа 400 или 401 или 403 при ошибке авторизации");
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @Description("ACTION после LOGOUT. Ожидаем ошибку.")
    @Step("ACTION после LOGOUT")
    @DisplayName("ACTION после LOGOUT должен вернуть ошибку")
    public void actionAfterLogout_shouldReturnError() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        ApiClient.sendPost(token, "LOGIN", Config.API_KEY);

        // LOGOUT
        Response logoutResponse =
                ApiClient.sendPost(token, "LOGOUT", Config.API_KEY);

        Allure.step("LOGOUT", () -> {
            Allure.addAttachment(
                    "HTTP Response Body LOGOUT",
                    logoutResponse.getBody().asString()
            );
            assertEquals(200, logoutResponse.getStatusCode());
        });

        // ACTION
        Response actionResponse =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("ACTION после LOGOUT", () -> {
            Allure.addAttachment(
                    "HTTP Response Body ACTION",
                    actionResponse.getBody().asString()
            );
            int status = actionResponse.getStatusCode();
            // Ожидается один из кодов 400, 401, 403 при ошибке авторизации после LOGOUT
            boolean validStatus = status == 400 || status == 401 || status == 403;
            assertEquals(true, validStatus, "Ожидается код ответа 400 или 401 или 403 при ошибке авторизации после LOGOUT");
        });
    }

    @Severity(SeverityLevel.NORMAL)
    @Test
    @Description("Повторный ACTION с тем же токеном. Ожидаем OK или idem.")
    @Step("Повторный ACTION")
    @DisplayName("Повторный ACTION с тем же токеном должен вернуть 200 или тот же результат")
    public void repeatedActionWithSameToken_shouldReturnOkOrSameResult() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        // Первый ACTION
        Response firstAction =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        // Второй ACTION
        Response secondAction =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка повторного ACTION", () -> {
            Allure.addAttachment(
                    "HTTP Response Body First ACTION",
                    firstAction.getBody().asString()
            );
            Allure.addAttachment(
                    "HTTP Response Body Second ACTION",
                    secondAction.getBody().asString()
            );
            assertEquals(200, firstAction.getStatusCode(), "Ожидается код ответа 200 при первом ACTION");
            assertEquals(200, secondAction.getStatusCode(), "Ожидается код ответа 200 при повторном ACTION с тем же токеном");
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @Description("ACTION при недоступности внешнего сервиса /auth.")
    @Step("ACTION при ошибке /auth")
    @DisplayName("ACTION при недоступности внешнего /auth сервиса должен вернуть 500")
    public void actionWhenAuthServiceUnavailable_shouldReturnServerError() {
        String token = TestDataGenerator.generateToken();

        wireMockServer.stubFor(post("/auth")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"ERROR\"}")));

        // /doAction остаётся доступным, ошибка эмулируется именно на этапе /auth

        Response response =
                ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ошибки сервера", () -> {
            int status = response.getStatusCode();
            Allure.addAttachment("HTTP Status Code", String.valueOf(status));
            Allure.addAttachment(
                    "HTTP Response Body",
                    response.getBody().asString()
            );
            assertEquals(500, status, "Ожидается код ответа 500 при недоступности /auth");
        });
    }

    @Severity(SeverityLevel.CRITICAL)
    @Test
    @Description("ACTION при недоступности внешнего сервиса /doAction.")
    @Step("ACTION при недоступности /doAction")
    @DisplayName("ACTION при недоступности внешнего /doAction сервиса должен вернуть 500")
    public void actionWhenDoActionServiceUnavailable_shouldReturnServerError() {
        String token = TestDataGenerator.generateToken();

        // LOGIN
        ApiClient.sendPost(token, "LOGIN", Config.API_KEY);
        lastUsedToken = token;

        // Эмулируем недоступность /doAction
        wireMockServer.stubFor(post("/doAction")
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"result\":\"ERROR\"}")));

        Response response = ApiClient.sendPost(token, "ACTION", Config.API_KEY);

        Allure.step("Проверка ошибки ACTION при недоступности /doAction", () -> {
            int status = response.getStatusCode();
            Allure.addAttachment("HTTP Status Code", String.valueOf(status));
            Allure.addAttachment(
                    "HTTP Response Body",
                    response.getBody().asString()
            );
            assertEquals(500, status, "Ожидается код ответа 500 при недоступности /doAction");
        });
    }
}
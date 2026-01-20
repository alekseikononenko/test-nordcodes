package com.autoqa.api.validation;

import com.autoqa.base.Config;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("API")
@Feature("Проверка обязательных заголовков и параметров")
@Story("Валидация токена и действия")
@DisplayName("Валидация запросов к API общая")
public class RequestValidationTest {

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = Config.BASE_URL;
    }

    @Step("Отправка HTTP-запроса к приложению: action={action}, token={token}, X-Api-Key={apiKey}")
    private Response sendRequest(String token, String action, String apiKey) {
        var request = RestAssured.given()
                .contentType("application/json")
                .body(buildBody(token, action));

        // добавляем заголовок только если apiKey не null
        if (apiKey != null) {
            request.header("X-Api-Key", apiKey);
        }

        return request.post("/api");
    }

    @Step("Формирование тела HTTP-запроса с параметрами action и token")
    private String buildBody(String token, String action) {
        String body = "{";
        if (action != null) body += "\"action\": \"" + action + "\"";
        if (token != null) {
            if (action != null) body += ",";
            body += "\"token\": \"" + token + "\"";
        }
        body += "}";
        return body;
    }

    @Test
    @DisplayName("Запрос без заголовка X-Api-Key должен вернуть 401 Unauthorized")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
Проверка валидации запроса без заголовка X-Api-Key.

Ожидаемое поведение:
- Приложение должно отклонить запрос
- HTTP-статус: 401 Unauthorized

Назначение теста:
Проверяет обязательность заголовка X-Api-Key для доступа к API.
""")
    void requestWithoutApiKey_shouldReturn401() {
        Response response = sendRequest("A823456789012345678901234567890B", "LOGIN", null);
        assertEquals(401, response.getStatusCode(), "Ожидается код 401 Unauthorized при отсутствии заголовка X-Api-Key");
    }

    @Test
    @DisplayName("Запрос с пустым значением заголовка X-Api-Key должен вернуть 401 Unauthorized")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
Проверка валидации запроса с пустым значением заголовка X-Api-Key.

Ожидаемое поведение:
- Приложение должно считать ключ некорректным
- HTTP-статус: 401 Unauthorized

Назначение теста:
Проверяет, что пустой API-ключ не допускается.
""")
    void requestWithEmptyApiKey_shouldReturn401() {
        Response response = sendRequest("A823456789012345678901234567890B", "LOGIN", "");
        assertEquals(401, response.getStatusCode(), "Ожидается код 401 Unauthorized при пустом значении X-Api-Key");
    }

    @Test
    @DisplayName("Запрос с неверным значением заголовка X-Api-Key должен вернуть 401 Unauthorized")
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
Проверка валидации запроса с неверным значением заголовка X-Api-Key.

Ожидаемое поведение:
- Приложение должно отклонить запрос
- HTTP-статус: 401 Unauthorized

Назначение теста:
Проверяет, что приложение не принимает произвольные API-ключи.
""")
    void requestWithInvalidApiKey_shouldReturn401() {
        Response response = sendRequest("A823456789012345678901234567890B", "LOGIN", "WRONG_KEY");
        assertEquals(401, response.getStatusCode(), "Ожидается код 401 Unauthorized при неверном значении X-Api-Key");
    }

    @Test
    @DisplayName("Запрос без параметра token должен вернуть 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
Проверка валидации запроса без параметра token.

Ожидаемое поведение:
- Приложение должно отклонить запрос как некорректный
- HTTP-статус: 400 Bad Request

Назначение теста:
Проверяет обязательность параметра token для выполнения операции LOGIN.
""")
    void requestWithoutToken_shouldReturn400() {
        Response response = sendRequest(null, "LOGIN", Config.API_KEY);
        assertEquals(400, response.getStatusCode(), "Ожидается код 400 Bad Request при отсутствии параметра token");
    }

    @Test
    @DisplayName("Запрос без параметра action должен вернуть 400 Bad Request")
    @Severity(SeverityLevel.NORMAL)
    @Description("""
Проверка валидации запроса без параметра action.

Ожидаемое поведение:
- Приложение должно отклонить запрос
- HTTP-статус: 400 Bad Request

Назначение теста:
Проверяет обязательность параметра action для обработки запроса.
""")
    void requestWithoutAction_shouldReturn400() {
        Response response = sendRequest("A823456789012345678901234567890B", null, Config.API_KEY);
        assertEquals(400, response.getStatusCode(), "Ожидается код 400 Bad Request при отсутствии параметра action");
    }
}
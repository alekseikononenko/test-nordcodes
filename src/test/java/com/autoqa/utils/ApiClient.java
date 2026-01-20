package com.autoqa.utils;

import com.autoqa.base.Config;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;

public class ApiClient {

    /**
     * Универсальный метод POST запроса
     * @param token токен пользователя
     * @param action действие пользователя (LOGIN, ACTION, LOGOUT)
     * @param apiKey API-ключ (можно Config.API_KEY или кастомный)
     */
    public static Response sendPost(String token, String action, String apiKey) {
        return given()
                .baseUri(Config.BASE_URL)
                .header("X-Api-Key", apiKey)
                .contentType("application/x-www-form-urlencoded")
                .formParam("token", token)
                .formParam("action", action)
                .accept("application/json")
                .when()
                .post()
                .then()
                .extract()
                .response();
    }
}
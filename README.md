# Тестовое задание AQA для Nord Codes

## Что тестируется
Автотесты проверяют Spring Boot приложение с одним эндпоинтом:
- POST http://localhost:8080/endpoint
- Параметры: token, action
- Заголовок: X-Api-Key

Внешний сервис (POST /auth и /doAction) эмулируется с помощью WireMock.

## Технологии
- Java 17
- Maven
- JUnit 5
- WireMock
- REST Assured
- Allure

## Как запускать
1. Запустите приложение:

`java -jar -Dsecret=qazWSXedc -Dmock=http://localhost:8888/ internal-0.0.1-SNAPSHOT.jar
`
2. Запустите тесты через Maven:

`mvn clean test`

3. Сгенерируйте и откройте отчет Allure:

`allure serve target/allure-results`
# API-автотесты для Restful-booker

Домашнее задание по курсу VK Education: автоматизация тестирования REST API.

Объект тестирования — учебный сервис бронирования отелей [Restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html).

## Стек

- **Kotlin** — язык тестов
- **RestAssured** (+ kotlin-extensions) — отправка HTTP-запросов и проверка ответов
- **JUnit 5** — запуск тестов
- **JSON Schema Validator** — контрактная проверка структуры ответа
- **Gradle** — сборка
- **GitHub Actions** — автозапуск тестов при push и pull request

## Как запустить

```bash
./gradlew test
```

HTML-отчёт после запуска: `build/reports/tests/test/index.html`

## Структура

```
src/test/kotlin/
├── BookingGetTest.kt        — получение броней (positive)
├── BookingCrudTest.kt       — создание и удаление брони (positive)
├── BookingNegativeTest.kt   — негативные, граничные и валидационные проверки
└── BookingContractTest.kt   — контрактный тест по JSON-схеме
src/test/resources/schemas/
└── booking-schema.json      — контракт ответа «бронь»
```

## Тесты

| # | Тест | Тип | Что проверяется |
|---|------|-----|-----------------|
| 1 | `list of bookings returns 200 and is not empty` | positive | Список броней возвращает 200, ответ в JSON, список не пустой, у элементов есть `bookingid` |
| 2 | `booking by id has firstname and lastname` | positive | Бронь по существующему id (берётся из списка) возвращает 200, ответ в JSON, есть `firstname` и `lastname` |
| 3 | `create booking returns id and saved data` | positive | Создание брони возвращает 200, выдан `bookingid`, сервер сохранил ровно те данные, что были отправлены |
| 4 | `deleted booking can not be found anymore` | positive | Своя бронь создаётся, удаляется с токеном (201), после удаления GET по её id возвращает 404 |
| 5 | `forbidden delete of booking without token` | negative | Удаление брони без токена отклоняется с кодом 403 |
| 6 | `impossible get of unexisting booking` | граница | Запрос брони с невозможным id (`-100`) возвращает 404 |
| 7 | `booking with no name returns 400` | валидация | Создание брони без обязательного поля `firstname` должно возвращать 400 (отключён, см. баги) |
| 8 | `incorrect password does not return token` | negative | При неверном пароле токен не выдаётся, в ответе `reason: Bad credentials` |
| 9 | `incorrect password returns 401` | negative | Неверный пароль должен возвращать 401 (отключён, см. баги) |
| 10 | `new booking matches json schema` | контракт | Ответ на запрос созданной брони соответствует JSON-схеме `booking-schema.json` |

## Контракт

Контракт — это договорённость между сервером и тем, кто им пользуется (сайтом, приложением, другими сервисами), о том,
какую форму будет иметь ответ. В `booking-schema.json` описан контракт ответа «бронь»:

- ответ — JSON-объект;
- обязательные поля: `firstname`, `lastname` (строки), `totalprice` (число), `depositpaid` (true/false), `bookingdates` (объект);
- внутри `bookingdates` обязательны `checkin` и `checkout` — строки в формате `ГГГГ-ММ-ДД`;
- `additionalneeds` — необязательная строка.

Тест `new booking matches json schema` проверяет весь ответ по этой схеме разом. Если сервер переименует поле,
удалит его или поменяет тип (например, начнёт присылать цену строкой), тест упадёт — и это заметят раньше,
чем сломаются приложения, которые этим API пользуются.

## Найденные баги и особенности API

| # | Что делали | Ожидалось | Что пришло |
|---|-----------|-----------|------------|
| 1 | Создание брони без `firstname` | `400 Bad Request` | `500 Internal Server Error` — сервер падает вместо понятного отказа (тест 7 отключён через `@Disabled`) |
| 2 | Запрос токена с неверным паролем | `401 Unauthorized` | `200 OK` с `{"reason":"Bad credentials"}`. Токен при этом не выдаётся — это правильно (тест 9 отключён через `@Disabled`) |
| 3 | `GET /booking/{id}` с неподдерживаемым `Accept` (например, как у браузера) | `406 Not Acceptable` | `418 I'm a Teapot` |
| 4 | `GET /booking/{id}` с `Accept: application/json, application/javascript, ...` (так отправляет `accept(ContentType.JSON)` в RestAssured) | JSON | `418` — сервер принимает только точное значение `application/json` |
| 5 | Удаление брони `DELETE /booking/{id}` | `200 OK` или `204 No Content` | `201 Created` (так указано и в документации) |
| 6 | Документация DeleteBooking | — | В таблице Header нет `Content-Type`, а в примере curl он есть: документация противоречит сама себе |

## Независимость тестов

Сервис общий: брони на нём создают и удаляют другие люди, а сам сервис периодически сбрасывает данные.
Поэтому тесты не используют захардкоженные id: бронь, записанная в тест вручную, может исчезнуть и тест станет нестабильным (flaky).

- тесты на чтение берут id существующей брони из списка `GET /booking`;
- тесты на удаление, контракт и негативные проверки сами создают себе бронь и работают только с ней — чужие данные не трогаются;
- для «несуществующей брони» используется id `-100`, который не может существовать.

Каждый тест можно запускать сколько угодно раз и в любом порядке.

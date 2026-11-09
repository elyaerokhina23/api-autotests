# REST API tests for Restful-booker

API test automation project from the VK Education QA Automation course (ITMO University, Master's program).

The system under test is [Restful-booker](https://restful-booker.herokuapp.com/apidoc/index.html), a public demo API for hotel bookings.

## Tech stack

- **Kotlin**
- **Rest Assured** with Kotlin extensions: HTTP requests and response checks
- **JUnit 5**: test runner
- **JSON Schema Validator**: contract testing
- **Gradle**: build
- **GitHub Actions**: tests run on every push and pull request

## How to run

```bash
./gradlew test
```

HTML report: `build/reports/tests/test/index.html`

## Project structure

```
src/test/kotlin/
├── BaseApiTest.kt           — shared test data, booking creation, token, cleanup after each test
├── BookingGetTest.kt        — get bookings (positive)
├── BookingCrudTest.kt       — create and delete a booking (positive)
├── BookingNegativeTest.kt   — negative, boundary and validation checks
└── BookingContractTest.kt   — contract test against a JSON schema
src/test/resources/schemas/
└── booking-schema.json      — booking response contract
```

## Tests

| # | Test | Type | What it checks |
|---|------|------|----------------|
| 1 | `list of bookings contains created booking` | positive | The test creates its own booking; the booking list returns 200 and JSON and contains the new `bookingid` |
| 2 | `booking by id returns its data` | positive | The test creates its own booking; GET by its id returns 200 and JSON, `firstname` and `lastname` match the data sent |
| 3 | `create booking returns id and saved data` | positive | Creating a booking returns 200 and a `bookingid` with the data that was sent; a follow-up GET by id confirms the booking was saved |
| 4 | `deleted booking can not be found anymore` | positive | A new booking is deleted with a token (201), then GET by its id returns 404 |
| 5 | `forbidden delete of booking without token` | negative | Deleting a booking without a token is rejected with 403; a follow-up GET confirms the booking still exists |
| 6 | `impossible get of unexisting booking` | boundary | A request with an impossible id (`-100`) returns 404 |
| 7 | `booking with no name returns 400` | validation | Creating a booking without the required `firstname` should return 400 (disabled, see bugs) |
| 8 | `incorrect password does not return token` | negative | A wrong password gives no token and returns `reason: Bad credentials` |
| 9 | `incorrect password returns 401` | negative | A wrong password should return 401 (disabled, see bugs) |
| 10 | `new booking matches json schema` | contract | The created booking matches `booking-schema.json` |

## Contract test

A contract is an agreement between the server and its clients (web, mobile apps, other services) about the shape of a response. `booking-schema.json` describes the booking response:

- the response is a JSON object;
- required fields: `firstname`, `lastname` (strings), `totalprice` (number), `depositpaid` (boolean), `bookingdates` (object);
- `bookingdates` must contain `checkin` and `checkout` as `YYYY-MM-DD` strings;
- `additionalneeds` is an optional string.

The test `new booking matches json schema` checks the whole response against this schema. If the server renames or removes a field, or changes its type (for example, sends the price as a string), the test fails before the clients break.

## Bugs and API quirks found

| # | Action | Expected | Actual |
|---|--------|----------|--------|
| 1 | Create a booking without `firstname` | `400 Bad Request` | `500 Internal Server Error`: the server crashes instead of a clear error (test 7 is `@Disabled`) |
| 2 | Request a token with a wrong password | `401 Unauthorized` | `200 OK` with `{"reason":"Bad credentials"}`. No token is issued, which is correct (test 9 is `@Disabled`) |
| 3 | `GET /booking/{id}` with an unsupported `Accept` header (as a browser sends) | `406 Not Acceptable` | `418 I'm a Teapot` |
| 4 | `GET /booking/{id}` with `Accept: application/json, application/javascript, ...` (what `accept(ContentType.JSON)` sends in Rest Assured) | JSON | `418`: the server accepts only the exact value `application/json` |
| 5 | Delete a booking with `DELETE /booking/{id}` | `200 OK` or `204 No Content` | `201 Created` (also stated in the docs) |
| 6 | DeleteBooking documentation | — | The Header table has no `Content-Type`, but the curl example uses it: the docs contradict themselves |

## Test independence

The service is shared: other people create and delete bookings, and the data is reset from time to time. So every test has its own test data and never relies on shared or hardcoded data:

- every test that needs a booking creates its own with `createBooking()` and works only with it;
- after each test, `cleanUp()` (annotated with `@AfterEach`) deletes every booking the test created, even if the test failed, so no test data is left on the server;
- the "non-existent booking" test uses id `-100`, which cannot exist.

Shared data and helpers (`baseUrl`, the booking body, `createBooking()`, `getToken()`, `cleanUp()`) live in the base class `BaseApiTest`, which all test classes extend.

Every test can run any number of times and in any order.

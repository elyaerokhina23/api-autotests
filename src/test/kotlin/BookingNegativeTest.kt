import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class BookingNegativeTest : BaseApiTest() {
    private val idUnexistingBooking = -100

    private val bookingWithNoName =
        """
        {
            "lastname" : "Brown",
            "totalprice" : 111,
            "depositpaid" : true,
            "bookingdates" : {
                "checkin" : "2018-01-01",
                "checkout" : "2019-01-01"
            },
            "additionalneeds" : "Breakfast"
        }
        """.trimIndent()

    private val badAuth =
        """
        {
            "username" : "admin",
            "password" : "password3"
        }
        """.trimIndent()

    @Test
    fun `forbidden delete of booking without token`() {
        val id = createBooking()

        Given {
            baseUri(baseUrl)
        } When {
            delete("/booking/$id")
        } Then {
            statusCode(403)
        }

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            body("firstname", equalTo("Jim"))
        }
    }

    @Test
    fun `impossible get of unexisting booking`() {
        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$idUnexistingBooking")
        } Then {
            statusCode(404)
        }
    }

    @Test
    @Disabled("БАГ: сервер возвращает 500 вместо 400")
    fun `booking with no name returns 400`() {
        Given {
            baseUri(baseUrl)
            contentType("application/json")
            accept("application/json")
            body(bookingWithNoName)
        } When {
            post("/booking")
        } Then {
            statusCode(400)
        }
    }

    @Test
    fun `incorrect password does not return token`() {
        Given {
            baseUri(baseUrl)
            contentType("application/json")
            body(badAuth)
        } When {
            post("/auth")
        } Then {
            body("token", nullValue())
            body("reason", equalTo("Bad credentials"))
        }
    }

    @Test
    @Disabled("БАГ: на неверный пароль сервер возвращает 200 вместо 401")
    fun `incorrect password returns 401`() {
        Given {
            baseUri(baseUrl)
            contentType("application/json")
            body(badAuth)
        } When {
            post("/auth")
        } Then {
            statusCode(401)
        }
    }
}

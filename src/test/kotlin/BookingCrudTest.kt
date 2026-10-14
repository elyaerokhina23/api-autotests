import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.equalTo
import org.junit.jupiter.api.Test

class BookingCrudTest {
    private val baseUrl = "https://restful-booker.herokuapp.com"

    private val newBooking =
        """
        {
            "firstname" : "Jim",
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

    private val newAuth =
        """
        {
            "username" : "admin",
            "password" : "password123"
        }
        """.trimIndent()

    @Test
    fun `create booking returns id and saved data`() {
        Given {
            baseUri(baseUrl)
            contentType("application/json")
            accept("application/json")
            body(newBooking)
        } When {
            post("/booking")
        } Then {
            statusCode(200)
            body("bookingid", notNullValue())
            body("booking.firstname", equalTo("Jim"))
            body("booking.totalprice", equalTo(111))
            body("booking.lastname", equalTo("Brown"))
            body("booking.depositpaid", equalTo(true))
            body("booking.bookingdates.checkin", equalTo("2018-01-01"))
        }
    }

    @Test
    fun `deleted booking can not be found anymore`() {
        val id: Int =
            Given {
                baseUri(baseUrl)
                contentType("application/json")
                accept("application/json")
                body(newBooking)
            } When {
                post("/booking")
            } Extract {
                path("bookingid")
            }

        val token: String =
            Given {
                baseUri(baseUrl)
                contentType("application/json")
                accept("application/json")
                body(newAuth)
            } When {
                post("/auth")
            } Extract {
                path("token")
            }

        Given {
            baseUri(baseUrl)
            contentType("application/json")
            header("Cookie", "token=$token")
        } When {
            delete("/booking/$id")
        } Then {
            statusCode(201)
        }

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(404)
        }
    }
}

import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.Test

class BookingCrudTest : BaseApiTest() {
    @Test
    fun `create booking returns id and saved data`() {
        val id: Int =
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
            } Extract {
                path("bookingid")
            }
        rememberForCleanUp(id)

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            body("firstname", equalTo("Jim"))
            body("lastname", equalTo("Brown"))
            body("totalprice", equalTo(111))
            body("depositpaid", equalTo(true))
            body("bookingdates.checkin", equalTo("2018-01-01"))
            body("bookingdates.checkout", equalTo("2019-01-01"))
        }
    }

    @Test
    fun `deleted booking can not be found anymore`() {
        val id = createBooking()
        val token = getToken()

        Given {
            baseUri(baseUrl)
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

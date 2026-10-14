import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test

class BookingContractTest {
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

    @Test
    fun `new booking matches json schema`() {
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

        Given {
            baseUri(baseUrl)
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
        }
    }
}

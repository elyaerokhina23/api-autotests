import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.greaterThan
import org.junit.jupiter.api.Test

class BookingGetTest {
    private val baseUrl = "https://restful-booker.herokuapp.com"

    @Test
    fun `list of bookings returns 200 and is not empty`() {
        Given {
            baseUri(baseUrl)
        } When {
            get("/booking")
        } Then {
            statusCode(200)
            header("Content-Type", containsString("application/json"))
            body("size()", greaterThan(0))
            body("[0].bookingid", notNullValue())
        }
    }

    @Test
    fun `booking by id has firstname and lastname`() {
        val id: Int =
            Given {
                baseUri(baseUrl)
            } When {
                get("/booking")
            } Extract {
                path("[0].bookingid")
            }

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            header("Content-Type", containsString("application/json"))
            body("firstname", notNullValue())
            body("lastname", notNullValue())
        }
    }
}

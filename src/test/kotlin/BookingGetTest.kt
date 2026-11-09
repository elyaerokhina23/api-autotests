import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.hamcrest.Matchers.containsString
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.Test

class BookingGetTest : BaseApiTest() {
    @Test
    fun `list of bookings contains created booking`() {
        val id = createBooking()

        Given {
            baseUri(baseUrl)
        } When {
            get("/booking")
        } Then {
            statusCode(200)
            header("Content-Type", containsString("application/json"))
            body("bookingid", hasItem(id))
        }
    }

    @Test
    fun `booking by id returns its data`() {
        val id = createBooking()

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            header("Content-Type", containsString("application/json"))
            body("firstname", equalTo("Jim"))
            body("lastname", equalTo("Brown"))
        }
    }
}

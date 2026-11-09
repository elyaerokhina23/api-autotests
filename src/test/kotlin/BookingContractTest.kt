import io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.Then
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.Test

class BookingContractTest : BaseApiTest() {
    @Test
    fun `new booking matches json schema`() {
        val id = createBooking()

        Given {
            baseUri(baseUrl)
            accept("application/json")
        } When {
            get("/booking/$id")
        } Then {
            statusCode(200)
            body(matchesJsonSchemaInClasspath("schemas/booking-schema.json"))
        }
    }
}

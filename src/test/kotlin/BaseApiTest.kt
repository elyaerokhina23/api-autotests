import io.restassured.module.kotlin.extensions.Extract
import io.restassured.module.kotlin.extensions.Given
import io.restassured.module.kotlin.extensions.When
import org.junit.jupiter.api.AfterEach

abstract class BaseApiTest {
    protected val baseUrl = "https://restful-booker.herokuapp.com"

    protected val newBooking =
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

    private val adminAuth =
        """
        {
            "username" : "admin",
            "password" : "password123"
        }
        """.trimIndent()

    private val createdBookingIds = mutableListOf<Int>()

    protected fun createBooking(): Int {
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
        rememberForCleanUp(id)
        return id
    }

    protected fun rememberForCleanUp(id: Int) {
        createdBookingIds.add(id)
    }

    protected fun getToken(): String =
        Given {
            baseUri(baseUrl)
            contentType("application/json")
            body(adminAuth)
        } When {
            post("/auth")
        } Extract {
            path("token")
        }

    @AfterEach
    fun cleanUp() {
        if (createdBookingIds.isEmpty()) return
        val token = getToken()
        createdBookingIds.forEach { id ->
            Given {
                baseUri(baseUrl)
                header("Cookie", "token=$token")
            } When {
                delete("/booking/$id")
            }
        }
    }
}

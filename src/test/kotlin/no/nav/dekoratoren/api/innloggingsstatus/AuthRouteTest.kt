package no.nav.dekoratoren.api.innloggingsstatus

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.kotest.assertions.json.shouldEqualJson
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService
import org.junit.jupiter.api.Test

class AuthRouteTest {
    private val oidcTokenService: OidcTokenService = mockk()
    private val subjectNameService: SubjectNameService = mockk()
    private val authTokenService = AuthTokenService(oidcTokenService, subjectNameService)

    private val routes = listOf("/person/nav-dekoratoren-api", "/person/innloggingsstatus")

    @Test
    fun `auth returns authenticated user as JSON on both routes`() = testAuthRoutes {
        every { oidcTokenService.getOidcToken(any()) } returns token
        coEvery { subjectNameService.getSubjectName(token.subject) } returns "Testbruker"

        for (route in routes) {
            val response = client.get("$route/auth")

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldEqualJson """
                {
                    "authenticated": true,
                    "name": "Testbruker",
                    "securityLevel": "4",
                    "userId": "123"
                }
            """.trimIndent()
        }
    }

    @Test
    fun `auth and summary omit user data when unauthenticated`() = testAuthRoutes {
        every { oidcTokenService.getOidcToken(any()) } returns null

        for (route in routes) {
            val authResponse = client.get("$route/auth")
            authResponse.status shouldBe HttpStatusCode.OK
            authResponse.bodyAsText() shouldEqualJson """{"authenticated": false}"""

            val summaryResponse = client.get("$route/summary")
            summaryResponse.status shouldBe HttpStatusCode.OK
            summaryResponse.bodyAsText() shouldEqualJson """{"authenticated": false}"""
        }
    }

    @Test
    fun `summary returns token details as JSON on both routes`() = testAuthRoutes {
        every { oidcTokenService.getOidcToken(any()) } returns token

        for (route in routes) {
            val response = client.get("$route/summary")

            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText() shouldEqualJson """
                {
                    "authenticated": true,
                    "authLevel": 4,
                    "oidc": {
                        "authLevel": 4,
                        "issueTime": "2024-01-02T10:15:30",
                        "expiryTime": "2024-01-02T11:15:30"
                    }
                }
            """.trimIndent()
        }
    }

    @Test
    fun `summary returns server error when token lookup fails`() = testAuthRoutes {
        every { oidcTokenService.getOidcToken(any()) } throws IllegalStateException("Token lookup failed")

        for (route in routes) {
            client.get("$route/summary").status shouldBe HttpStatusCode.InternalServerError
        }
    }

    private fun testAuthRoutes(block: suspend io.ktor.server.testing.ApplicationTestBuilder.() -> Unit) =
        testApplication {
            application {
                install(ContentNegotiation) {
                    jackson {
                        registerModule(JavaTimeModule())
                        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    }
                }
                routing {
                    for (routePath in routes) {
                        route(routePath) {
                            auth(authTokenService)
                        }
                    }
                }
            }
            block()
        }

    private val token = OidcTokenInfo(
        subject = "123",
        authLevel = 4,
        issueTime = LocalDateTime.parse("2024-01-02T10:15:30"),
        expiryTime = LocalDateTime.parse("2024-01-02T11:15:30")
    )
}

package no.nav.dekoratoren.api.varsel

import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.*
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.ktor.util.KtorDsl
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthInfo
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be less than`
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.system.measureTimeMillis

internal class VarselApiTest {

    private val authService: AuthTokenService = mockk()
    private val tokenFetcher: VarselbjelleTokenFetcher = mockk()

    private val ident = "123"
    private val level = 4
    private val varselbjelleUrl = "http://varselbjelle-api"
    private val sammendrag = "sammendrag"

    private val doneDelay = 3000L

    @AfterEach
    fun cleanup() {
        clearMocks(authService, tokenFetcher)
    }

    @Test
    fun `Henter varselSammendrag fra varselbjelle-api`() = testVarselApi {


        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/rest/varsel/hentsiste")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status `should be equal to` HttpStatusCode.OK
        result.readBytes() shouldBeEqualTo sammendrag.encodeToByteArray()
    }

    @Test
    fun `Videresender erlest til varselbjelle-api`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val id = "654"

        val result = client.request {
            url("/rest/varsel/erlest/$id")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status `should be equal to` HttpStatusCode.OK
        result.readBytes() shouldBeEqualTo id.encodeToByteArray()
    }

    @Test
    fun `Proxyer get-kall til vilkårlige endepunkt hos varselbjelle-api`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Svarer med 401 hvis bruker ikke er autentisert`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns unauthenticated()

        client.request {
            url("/rest/varsel/hentsiste")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBeEqualTo HttpStatusCode.Unauthorized

        client.request {
            url("/rest/varsel/erlest/123")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBeEqualTo HttpStatusCode.Unauthorized

        client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBeEqualTo HttpStatusCode.Unauthorized

        client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBeEqualTo HttpStatusCode.Unauthorized
    }

    @Test
    fun `Proxyer post-kall til vilkårlige endepunkt hos varselbjelle-api`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status `should be equal to` HttpStatusCode.OK
    }

    @Test
    fun `Svarer med 400 hvis path ikke fantes hos varselbjelle-api`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/finnes/ikke")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status `should be equal to` HttpStatusCode.BadRequest
    }

    @Test
    fun `Svarer umiddelbart ved kall til eget done-endepunkt`() = testVarselApi {
        every { authService.fetchAndParseAuthInfo(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        lateinit var response: HttpResponse

        val elapsed = measureTimeMillis {
            response = client.request {
                url("/varsel/beskjed/done")
                method = HttpMethod.Post
            }
        }

        response.status `should be equal to` HttpStatusCode.Accepted
        elapsed `should be less than` doneDelay
    }

    private fun authenticated(ident: String, level: Int): AuthInfo {
        val tokenInfo = OidcTokenInfo(
            subject = ident,
            authLevel = level,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusHours(1)
        )

        return AuthInfo(tokenInfo)
    }

    private fun unauthenticated() = AuthInfo(null)

    @KtorDsl
    private fun testVarselApi(block: suspend ApplicationTestBuilder.(VarselbjelleConsumer) -> Unit) = testApplication {
        val varselbjelleConsumer = VarselbjelleConsumer(varselbjelleUrl, client, tokenFetcher)

        application {
            routing {
                varselApi(authService, varselbjelleConsumer)
            }
        }

        externalServices {
            hosts(varselbjelleUrl) {
                routing {
                    get("/varsel/sammendrag") {
                        call.respond(HttpStatusCode.OK, sammendrag)
                    }

                    get("/annet/endepunkt") {
                        call.respond(HttpStatusCode.OK, "ok")
                    }

                    post("/varsel/erlest/{id}") {
                        call.respond(HttpStatusCode.OK, call.parameters["id"]?:"")
                    }

                    post("/annet/endepunkt") {
                        call.respond(HttpStatusCode.OK, "ok")
                    }

                    post("/varsel/beskjed/done") {
                        delay(doneDelay)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }

        this.block(varselbjelleConsumer)
    }
}

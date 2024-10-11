package no.nav.dekoratoren.api.varsel

import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readBytes
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
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.delay
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class VarselApiTest {

    private val oidcTokenService: OidcTokenService = mockk()
    private val tokenFetcher: VarselbjelleTokenFetcher = mockk()

    private val ident = "123"
    private val level = 4
    private val dummyDateTime = LocalDateTime.now()
    private val varselbjelleUrl = "http://varselbjelle-api"
    private val sammendrag = "sammendrag"

    private val doneDelay = 3000L

    @AfterEach
    fun cleanup() {
        clearMocks(oidcTokenService, tokenFetcher)
    }

    @Test
    fun `Henter varselSammendrag fra varselbjelle-api`() = testVarselApi {

        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/rest/varsel/hentsiste")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status shouldBe HttpStatusCode.OK
        result.readBytes() shouldBe sammendrag.encodeToByteArray()
    }

    @Test
    fun `Videresender erlest til varselbjelle-api`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val id = "654"

        val result = client.request {
            url("/rest/varsel/erlest/$id")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status shouldBe HttpStatusCode.OK
        result.readBytes() shouldBe id.encodeToByteArray()
    }

    @Test
    fun `Proxyer get-kall til vilkårlige endepunkt hos varselbjelle-api`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status shouldBe HttpStatusCode.OK
    }

    @Test
    fun `Svarer med 401 hvis bruker ikke er autentisert`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns unauthenticated()

        client.request {
            url("/rest/varsel/hentsiste")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBe HttpStatusCode.Unauthorized

        client.request {
            url("/rest/varsel/erlest/123")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBe HttpStatusCode.Unauthorized

        client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Get
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBe HttpStatusCode.Unauthorized

        client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }.status shouldBe HttpStatusCode.Unauthorized
    }

    @Test
    fun `Proxyer post-kall til vilkårlige endepunkt hos varselbjelle-api`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/annet/endepunkt")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status shouldBe HttpStatusCode.OK
    }

    @Test
    fun `Svarer med 400 hvis path ikke fantes hos varselbjelle-api`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        val result = client.request {
            url("/varsel/proxy/finnes/ikke")
            method = HttpMethod.Post
            header(HttpHeaders.Authorization, "dummy")
        }

        result.status shouldBe HttpStatusCode.BadRequest
    }

    @Test
    fun `Svarer umiddelbart ved kall til eget done-endepunkt`() = testVarselApi {
        every { oidcTokenService.getOidcToken(any()) } returns authenticated(ident, level)
        coEvery { tokenFetcher.fetchToken() } returns "token"

        lateinit var response: HttpResponse

        val elapsed = measureTimeMillis {
            response = client.request {
                url("/varsel/beskjed/done")
                method = HttpMethod.Post
            }
        }

        response.status shouldBe HttpStatusCode.Accepted
        elapsed shouldBeLessThan doneDelay
    }

    private fun authenticated(subject: String, authLevel: Int) = OidcTokenInfo(
        subject = subject,
        authLevel = authLevel,
        expiryTime = dummyDateTime,
        issueTime = dummyDateTime
    )

    private fun unauthenticated() = null

    @KtorDsl
    private fun testVarselApi(block: suspend ApplicationTestBuilder.(VarselbjelleConsumer) -> Unit) = testApplication {
        val varselbjelleConsumer = VarselbjelleConsumer(varselbjelleUrl, client, tokenFetcher)

        application {
            routing {
                varsel(oidcTokenService, varselbjelleConsumer)
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
                        call.respond(HttpStatusCode.OK, call.parameters["id"] ?: "")
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

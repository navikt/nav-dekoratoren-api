package no.nav.dekoratoren.api.innloggingsstatus.oidc

import io.kotest.assertions.assertSoftly
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.date.shouldBeAfter
import io.kotest.matchers.date.shouldBeBefore
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.dekoratoren.api.config.Environment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OidcTokenServiceTest {

    private val oidcTokenValidator: OidcTokenValidator = mockk()
    private val environment: Environment = mockk()

    private val oidcTokenService = OidcTokenService(oidcTokenValidator, environment)

    private val call: ApplicationCall = mockk()

    @BeforeEach
    fun setupCommonMocks() {
        every { environment.oidcIssuer } returns ISSUER
    }

    @Test
    fun `should extract correct information from a jwt token when identity is in 'sub'`() {
        val jwtToken = JwtTokenObjectMother.createJwtToken(identityClaim = "sub")

        every { environment.identityClaim } returns "sub"
        every { oidcTokenValidator.getValidToken(call, ISSUER) } returns jwtToken

        val oidcTokenInfo = requireNotNull(oidcTokenService.getOidcToken(call))

        assertSoftly(oidcTokenInfo) {
            subject shouldBe "1234"
            authLevel shouldBe 3
            issueTime shouldBeBefore LocalDateTime.now()
            expiryTime shouldBeAfter LocalDateTime.now()
        }
    }

    @Test
    fun `should extract correct information from a jwt token when identity is in 'pid'`() {
        val jwtToken = JwtTokenObjectMother.createJwtToken(identityClaim = "pid")

        every { environment.identityClaim } returns "pid"
        every { oidcTokenValidator.getValidToken(call, ISSUER) } returns jwtToken

        val oidcTokenInfo = requireNotNull(oidcTokenService.getOidcToken(call))

        assertSoftly(oidcTokenInfo) {
            subject shouldBe "1234"
            authLevel shouldBe 3
            issueTime shouldBeBefore LocalDateTime.now()
            expiryTime shouldBeAfter LocalDateTime.now()
        }
    }


    @Test
    fun `should throw exception when no identity is found for requested claim`() {
        val jwtToken = JwtTokenObjectMother.createJwtToken()

        every { environment.identityClaim } returns "pid"
        every { oidcTokenValidator.getValidToken(call, ISSUER) } returns jwtToken

        shouldThrow<RuntimeException> { oidcTokenService.getOidcToken(call) }
    }

    companion object {
        private const val ISSUER = "issuer"
    }
}
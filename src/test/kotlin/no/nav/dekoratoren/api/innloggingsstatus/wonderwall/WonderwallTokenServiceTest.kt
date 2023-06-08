package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import no.nav.dekoratoren.api.common.toUtcDateTime
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.innloggingsstatus.oidc.JwtTokenObjectMother
import org.amshove.kluent.`should be before`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class WonderwallTokenServiceTest {

    private val wonderwallTokenValidator: WonderwallTokenValidator = mockk()
    private val environment: Environment = mockk()

    private val wonderwallTokenService = WonderwallTokenService(
        wonderwallTokenValidator,
        environment
    )

    private val call: ApplicationCall = mockk()

    @Test
    fun `should return OidcUserInfo if valid idporten token found`() {
        val securityLevel = 4
        val subject = "123457890"
        val idportenToken = JwtTokenObjectMother.idportenToken(subject = subject, level = securityLevel)

        every { environment.idportenIdentityClaim } returns "pid"
        every { wonderwallTokenValidator.getAuthHeaderToken(call) } returns idportenToken

        val oidcTokenInfo = wonderwallTokenService.getToken(call)

        with(oidcTokenInfo) {
            `should not be null`()
            subject `should be equal to` subject
            authLevel `should be equal to` securityLevel
            issueTime `should be before` LocalDateTime.now()
            expiryTime `should be equal to` idportenToken.jwtTokenClaims.expirationTime.toUtcDateTime()
        }
    }

    @Test
    fun `should return null if no valid token found`() {
        every { wonderwallTokenValidator.getAuthHeaderToken(call) } returns null

        val oidcTokenInfo = wonderwallTokenService.getToken(call)

        oidcTokenInfo.`should be null`()
    }
}

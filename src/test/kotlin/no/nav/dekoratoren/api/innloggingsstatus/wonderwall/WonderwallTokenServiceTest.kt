package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import io.ktor.server.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import java.time.LocalDateTime
import no.nav.dekoratoren.api.common.toUtcDateTime
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.innloggingsstatus.oidc.JwtTokenObjectMother
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.dekoratoren.api.innloggingsstatus.wonderwall.SelfIssuedTokenObjectMother.claims
import org.amshove.kluent.`should be before`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be in range`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.fail
import org.junit.jupiter.api.Test

class WonderwallTokenServiceTest {

    private val selfIssuedTokenIssuer: SelfIssuedTokenIssuer = mockk()
    private val wonderwallTokenValidator: WonderwallTokenValidator = mockk()
    private val oidcTokenValidator: OidcTokenValidator = mockk()
    private val environment: Environment = mockk()

    private val wonderwallTokenService = WonderwallTokenService(
        wonderwallTokenValidator,
        selfIssuedTokenIssuer,
        oidcTokenValidator,
        environment
    )

    private val call: ApplicationCall = mockk()
    private val key = generateRandomKey()

    @Test
    fun `should return OidcUserInfo if valid self issued token found`() {
        val securityLevel = 4
        val subject = "123457890"
        val claims = claims(subject = subject, securityLevel = "Level${securityLevel}")
        val selfIssuedToken = SelfIssuedTokenObjectMother.generate(key, claims)

        every { wonderwallTokenValidator.getAuthHeaderToken(call) } returns null
        every { wonderwallTokenValidator.getSelfIssuedToken(call) } returns selfIssuedToken

        val oidcTokenInfo = wonderwallTokenService.getToken(call)

        with(oidcTokenInfo) {
            `should not be null`()
            subject `should be equal to` subject
            authLevel `should be equal to` securityLevel
            issueTime `should be before` LocalDateTime.now()
            expiryTime `should be equal to` selfIssuedToken.jwtTokenClaims.expirationTime.toUtcDateTime()
        }
    }

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
        every { wonderwallTokenValidator.getSelfIssuedToken(call) } returns null
        every { wonderwallTokenValidator.getAuthHeaderToken(call) } returns null

        val oidcTokenInfo = wonderwallTokenService.getToken(call)

        oidcTokenInfo.`should be null`()
    }

    @Test
    fun `should exchange valid ID-porten token and user info with self issued token`() {
        val idportenToken = JwtTokenObjectMother.idportenToken()

        val key = generateRandomKey()
        val selfIssuedToken = SelfIssuedTokenObjectMother.generate(key, claims())

        val idportenIssuer = "idporten"
        val idportenIdentityClaim = "pid"

        every { environment.oidcIssuer } returns idportenIssuer
        every { environment.idportenIdentityClaim } returns idportenIdentityClaim
        every { oidcTokenValidator.getValidToken(call, idportenIssuer) } returns idportenToken
        every { selfIssuedTokenIssuer.issueToken(idportenToken) } returns selfIssuedToken

        when (val response = wonderwallTokenService.exchangeToken(call)) {
            is SelfIssuedTokenResponse.Invalid -> fail("Expected $response to be an instance of ${SelfIssuedTokenResponse.OK::class}")
            is SelfIssuedTokenResponse.OK -> {
                response.token `should be equal to` selfIssuedToken.tokenAsString
                response.expiresIn `should be in range` (1730..1800)
            }
        }
    }

    @Test
    fun `should return access denied if attempting to exchange invalid ID-porten token`() {
        val issuer = "idporten"
        every { environment.oidcIssuer } returns issuer
        every { oidcTokenValidator.getValidToken(call, issuer) } returns null

        when (val response = wonderwallTokenService.exchangeToken(call)) {
            is SelfIssuedTokenResponse.OK -> fail("Expected $response to be an instance of ${SelfIssuedTokenResponse.Invalid::class}")
            is SelfIssuedTokenResponse.Invalid -> {
                response.error `should be equal to` "access_denied"
                response.errorDescription `should be equal to` "Authorization header does not contain a valid ID-porten token."
            }
        }
    }
}

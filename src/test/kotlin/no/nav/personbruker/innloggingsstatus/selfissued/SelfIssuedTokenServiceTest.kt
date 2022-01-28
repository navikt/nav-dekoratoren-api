package no.nav.personbruker.innloggingsstatus.selfissued

import com.nimbusds.oauth2.sdk.ErrorObject
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.mockk.every
import io.mockk.mockk
import no.nav.personbruker.innloggingsstatus.common.toUtcDateTime
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.personbruker.innloggingsstatus.oidc.JwtTokenObjectMother
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenObjectMother.claims
import org.amshove.kluent.`should be before`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should not be null`
import org.amshove.kluent.fail
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SelfIssuedTokenServiceTest {

    private val selfIssuedTokenIssuer: SelfIssuedTokenIssuer = mockk()
    private val selfIssuedTokenValidator: SelfIssuedTokenValidator = mockk()
    private val oidcTokenValidator: OidcTokenValidator = mockk()
    private val environment: Environment = mockk()

    private val selfIssuedTokenService = SelfIssuedTokenService(
        selfIssuedTokenValidator,
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

        every { selfIssuedTokenValidator.getValidToken(call) } returns selfIssuedToken

        val oidcTokenInfo = selfIssuedTokenService.getSelfIssuedToken(call)

        with(oidcTokenInfo) {
            `should not be null`()
            subject `should be equal to` subject
            authLevel `should be equal to` securityLevel
            issueTime `should be before` LocalDateTime.now()
            expiryTime `should be equal to` selfIssuedToken.jwtTokenClaims.expirationTime.toUtcDateTime()
        }
    }

    @Test
    fun `should return null if no valid self issued token found`() {
        every { selfIssuedTokenValidator.getValidToken(call) } returns null

        val oidcTokenInfo = selfIssuedTokenService.getSelfIssuedToken(call)

        oidcTokenInfo.`should be null`()
    }

    @Test
    fun `should exchange valid ID-porten token and user info with self issued token`() {
        val idportenToken = JwtTokenObjectMother.idportenToken()

        val key = generateRandomKey()
        val selfIssuedToken = SelfIssuedTokenObjectMother.generate(key, claims())

        val idportenIssuer = "idporten"
        val idportenIdentityClaim = "pid"

        every { environment.idportenIssuer } returns idportenIssuer
        every { environment.idportenIdentityClaim } returns idportenIdentityClaim
        every { oidcTokenValidator.getValidToken(call, idportenIssuer) } returns idportenToken
        every { selfIssuedTokenIssuer.issueToken(idportenToken) } returns selfIssuedToken

        val response = selfIssuedTokenService.exchangeToken(call)

        response `should be instance of` SelfIssuedTokenResponse.OK::class
    }

    @Test
    fun `should return access denied if attempting to exchange invalid ID-porten token`() {
        val issuer = "idporten"
        every { environment.idportenIssuer } returns issuer
        every { oidcTokenValidator.getValidToken(call, issuer) } returns null

        when (val response = selfIssuedTokenService.exchangeToken(call)) {
            is SelfIssuedTokenResponse.OK -> fail("Expected $response to be an instance of ${SelfIssuedTokenResponse.Invalid::class}")
            is SelfIssuedTokenResponse.Invalid -> {
                response.error `should be instance of` ErrorObject::class
                response.error.code `should be equal to` "access_denied"
                response.error.description `should be equal to` "Authorization header does not contain a valid ID-porten token."
                response.error.httpStatusCode `should be equal to` HttpStatusCode.Forbidden.value
            }
        }
    }
}

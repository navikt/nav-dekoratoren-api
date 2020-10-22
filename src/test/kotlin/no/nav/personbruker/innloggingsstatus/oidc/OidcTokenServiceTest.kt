package no.nav.personbruker.innloggingsstatus.oidc

import io.ktor.application.ApplicationCall
import io.ktor.util.KtorExperimentalAPI
import io.mockk.every
import io.mockk.mockk
import no.nav.personbruker.innloggingsstatus.config.Environment
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should throw`
import org.amshove.kluent.invoking
import org.junit.jupiter.api.Test
import java.lang.RuntimeException
import java.time.LocalDateTime
import java.time.ZoneOffset

@KtorExperimentalAPI
internal class OidcTokenServiceTest {

    val oidcTokenValidator: OidcTokenValidator = mockk()
    val environment: Environment = mockk()

    val oidcTokenService = OidcTokenService(oidcTokenValidator, environment)

    val call: ApplicationCall = mockk()

    @Test
    fun `should extract correct information from a jwt token when identity is in 'sub'`() {
        val subject = "1234"
        val level = 3
        val issueTime = LocalDateTime.now()
        val expiryTime = issueTime.plusHours(1)

        val jwtToken = JwtTokenObjectMother.createJwtToken(subject, level, issueTime, expiryTime, identityClaim = "sub")

        every { environment.identityClaim } returns "sub"
        every { oidcTokenValidator.getValidToken(call) } returns jwtToken

        val oidcTokenInfo = oidcTokenService.getOidcToken(call)

        oidcTokenInfo?.subject `should be equal to` subject
        oidcTokenInfo?.authLevel `should be equal to` level
        oidcTokenInfo?.issueTime?.toEpochSecond(ZoneOffset.UTC) `should be equal to` issueTime.toEpochSecond(ZoneOffset.UTC)
        oidcTokenInfo?.expiryTime?.toEpochSecond(ZoneOffset.UTC) `should be equal to` expiryTime.toEpochSecond(ZoneOffset.UTC)
    }

    @Test
    fun `should extract correct information from a jwt token when identity is in 'pid'`() {
        val subject = "1234"
        val level = 3
        val issueTime = LocalDateTime.now()
        val expiryTime = issueTime.plusHours(1)

        val jwtToken = JwtTokenObjectMother.createJwtToken(subject, level, issueTime, expiryTime, identityClaim = "pid")

        every { environment.identityClaim } returns "pid"
        every { oidcTokenValidator.getValidToken(call) } returns jwtToken

        val oidcTokenInfo = oidcTokenService.getOidcToken(call)

        oidcTokenInfo?.subject `should be equal to` subject
        oidcTokenInfo?.authLevel `should be equal to` level
        oidcTokenInfo?.issueTime?.toEpochSecond(ZoneOffset.UTC) `should be equal to` issueTime.toEpochSecond(ZoneOffset.UTC)
        oidcTokenInfo?.expiryTime?.toEpochSecond(ZoneOffset.UTC) `should be equal to` expiryTime.toEpochSecond(ZoneOffset.UTC)
    }


    @Test
    fun `should throw exception when no identity is found for requested claim`() {
        val subject = "1234"
        val level = 3
        val issueTime = LocalDateTime.now()
        val expiryTime = issueTime.plusHours(1)

        val jwtToken = JwtTokenObjectMother.createJwtToken(subject, level, issueTime, expiryTime, identityClaim = "sub")

        every { environment.identityClaim } returns "pid"
        every { oidcTokenValidator.getValidToken(call) } returns jwtToken

        invoking { oidcTokenService.getOidcToken(call) } `should throw` RuntimeException::class
    }
}
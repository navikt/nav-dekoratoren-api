package no.nav.personbruker.innloggingsstatus.selfissued

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.ktor.application.ApplicationCall
import io.mockk.every
import io.mockk.mockk
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenObjectMother.DEFAULT_ISSUER
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenObjectMother.DEFAULT_SECURITY_LEVEL
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenObjectMother.DEFAULT_SUBJECT
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should contain`
import org.amshove.kluent.`should not be null`
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Date

private const val DEFAULT_COOKIE_NAME = "some-cookie"

class SelfIssuedTokenValidatorTest {
    private val environment: Environment = mockk()
    private val call: ApplicationCall = mockk()

    init {
        every { environment.selfIssuedIssuer } returns DEFAULT_ISSUER
        every { environment.selfIssuedCookieName } returns DEFAULT_COOKIE_NAME
    }

    @Test
    fun `should return JwtToken when given a valid token`() {
        initMocks()

        val selfIssuedTokenValidator = SelfIssuedTokenValidator(environment)
        val validJwt = selfIssuedTokenValidator.getValidToken(call)

        validJwt.`should not be null`()

        val signedJwt = SignedJWT.parse(validJwt.tokenAsString)
        val now = Date.from(Instant.now())

        with(signedJwt.jwtClaimsSet) {
            issuer `should be equal to` DEFAULT_ISSUER
            subject `should be equal to` DEFAULT_SUBJECT
            audience `should contain` DEFAULT_ISSUER

            getStringClaim(SelfIssuedTokenIssuer.CLAIM_SECURITY_LEVEL) `should be equal to` DEFAULT_SECURITY_LEVEL

            issueTime.before(now).`should be true`()
        }
    }

    @Test
    fun `should return null when given a token with invalid key`() {
        initMocks()

        // rotate key for validator to trigger invalid token signature during verification
        every { environment.selfIssuedSecretKey } returns generateRandomKey()

        assertInvalidToken()
    }

    @Test
    fun `should return null when given a token with invalid issuer`() {
        val issuer = "invalid"
        initMocks(claims = SelfIssuedTokenObjectMother.claims(issuer = issuer))
        assertInvalidToken()
    }

    @Test
    fun `should return null when given a token with invalid audience`() {
        val audience = "invalid"
        initMocks(claims = SelfIssuedTokenObjectMother.claims(audience = audience))
        assertInvalidToken()
    }

    @Test
    fun `should return null when given a token with expiry time in the past`() {
        val expiry = Date.from(Instant.now().minus(1, ChronoUnit.MINUTES))
        initMocks(claims = SelfIssuedTokenObjectMother.claims(expiry = expiry))
        assertInvalidToken()
    }

    @Test
    fun `should return null when given a token with issue time in the future`() {
        val issueTime = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES))
        initMocks(claims = SelfIssuedTokenObjectMother.claims(issueTime = issueTime))
        assertInvalidToken()
    }

    private fun initMocks(
        claims: JWTClaimsSet = SelfIssuedTokenObjectMother.claims(
            issuer = DEFAULT_ISSUER,
            audience = DEFAULT_ISSUER
        )
    ) {
        val key = generateRandomKey()
        every { environment.selfIssuedSecretKey } returns key

        val jwt = SelfIssuedTokenObjectMother.generate(key, claims)
        val jwtSerialized = jwt.tokenAsString
        every { call.request.cookies["some-cookie"] } returns jwtSerialized
    }

    private fun assertInvalidToken() {
        val selfIssuedTokenValidator = SelfIssuedTokenValidator(environment)
        val invalidJwt = selfIssuedTokenValidator.getValidToken(call)

        invalidJwt.`should be null`()
    }
}

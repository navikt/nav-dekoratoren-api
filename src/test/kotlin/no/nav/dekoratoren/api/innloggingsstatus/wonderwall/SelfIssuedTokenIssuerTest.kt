package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import com.nimbusds.jwt.SignedJWT
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.*
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.innloggingsstatus.oidc.JwtTokenObjectMother
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.invoking
import org.amshove.kluent.shouldThrow
import org.amshove.kluent.withMessage
import org.junit.jupiter.api.Test

class SelfIssuedTokenIssuerTest {
    private val environment: Environment = mockk()
    private val selfIssuedIssuer = "self-issued"

    @Test
    fun `should issue a valid token given a key of sufficient length for the HS512 algorithm`() {
        val idportenIdentityClaim = "pid"
        val subjectToken = JwtTokenObjectMother.idportenToken()
        val key = generateRandomKey()

        every { environment.selfIssuedSecretKey } returns key
        every { environment.selfIssuedIssuer } returns selfIssuedIssuer
        every { environment.idportenIdentityClaim } returns idportenIdentityClaim

        val selfIssuedTokenIssuer = SelfIssuedTokenIssuer(environment)

        val jwt = selfIssuedTokenIssuer.issueToken(subjectToken)
        val now = Date.from(Instant.now())

        val signedJwt = SignedJWT.parse(jwt.tokenAsString)

        with(signedJwt.jwtClaimsSet) {
            issuer `should be equal to` selfIssuedIssuer
            subject `should be equal to` subjectToken.jwtTokenClaims.getStringClaim(idportenIdentityClaim)
            audience `should be equal to` listOf(selfIssuedIssuer)

            getStringClaim(SelfIssuedTokenIssuer.CLAIM_SECURITY_LEVEL) `should be equal to` "Level4"

            issueTime.before(now).`should be true`()
            expirationTime.`should be equal to`(subjectToken.jwtTokenClaims.expirationTime)
        }
    }

    @Test
    fun `should throw an exception if the key does not have a sufficient length for the HS512 algorithm`() {
        val key = generateRandomKey(63) // HS512 requires a key with size at least 512 bits / 64 bytes
        every { environment.selfIssuedSecretKey } returns key
        every { environment.selfIssuedIssuer } returns selfIssuedIssuer

        invoking { SelfIssuedTokenIssuer(environment) } shouldThrow RuntimeException::class withMessage "Invalid secret key length (63). Must be at least 64 bytes."
    }
}

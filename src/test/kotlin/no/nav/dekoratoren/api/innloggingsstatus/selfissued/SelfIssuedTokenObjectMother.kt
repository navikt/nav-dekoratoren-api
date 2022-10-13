package no.nav.dekoratoren.api.innloggingsstatus.selfissued

import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import no.nav.security.token.support.core.jwt.JwtToken

object SelfIssuedTokenObjectMother {

    const val DEFAULT_ISSUER = "issuer"
    const val DEFAULT_SUBJECT = "12345689"
    const val DEFAULT_SECURITY_LEVEL = "Level4"

    fun generate(secret: String, claims: JWTClaimsSet): JwtToken {
        val signer: JWSSigner = MACSigner(secret)
        val header = JWSHeader.Builder(SelfIssuedTokenIssuer.signatureAlgorithm).build()
        val signedJwt = SignedJWT(header, claims)
            .apply { this.sign(signer) }
        return JwtToken(signedJwt.serialize())
    }

    fun claims(
        issuer: String = DEFAULT_ISSUER,
        audience: String = DEFAULT_ISSUER,
        expiry: Date = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES)),
        issueTime: Date = Date.from(Instant.now()),
        subject: String = DEFAULT_SUBJECT,
        securityLevel: String = DEFAULT_SECURITY_LEVEL,
    ): JWTClaimsSet {
        return JWTClaimsSet.Builder()
            .issuer(issuer)
            .expirationTime(expiry)
            .issueTime(issueTime)
            .audience(audience)
            .claim(SelfIssuedTokenIssuer.CLAIM_IDENTITY, subject)
            .claim(SelfIssuedTokenIssuer.CLAIM_SECURITY_LEVEL, securityLevel)
            .build()
    }
}

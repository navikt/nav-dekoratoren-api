package no.nav.personbruker.innloggingsstatus.selfissued

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import java.time.Instant
import java.util.Date
import java.util.UUID

class SelfIssuedTokenIssuer(private val environment: Environment) {
    private val signer: JWSSigner = MACSigner(environment.selfIssuedSecretKey)

    init {
        val keySize = environment.selfIssuedSecretKey.length
        if (keySize < 64) {
            throw RuntimeException("Invalid secret key length (${keySize}). Must be at least 64 bytes.")
        }
    }

    fun issueToken(subjectToken: JwtToken): JwtToken {
        val claimsSet = claimsSet(subjectToken)
        val header = JWSHeader.Builder(signatureAlgorithm).build()
        val signedJwt = SignedJWT(header, claimsSet)
            .apply { this.sign(signer) }
        return JwtToken(signedJwt.serialize())
    }

    private fun claimsSet(subjectToken: JwtToken): JWTClaimsSet {
        val issuer: String = environment.selfIssuedIssuer
        val targetAudience: String = environment.selfIssuedIssuer

        val subjectTokenClaims: JwtTokenClaims = subjectToken.jwtTokenClaims
        val expiry: Date = subjectTokenClaims.expirationTime
        val subject = subjectTokenClaims.getStringClaim(environment.idportenIdentityClaim)
        val securityLevel = subjectTokenClaims.getStringClaim(CLAIM_SECURITY_LEVEL)

        val now = Instant.now()
        return JWTClaimsSet.Builder()
            .issuer(issuer)
            .expirationTime(expiry)
            .issueTime(Date.from(now))
            .jwtID(UUID.randomUUID().toString())
            .audience(targetAudience)
            .claim(CLAIM_IDENTITY, subject)
            .claim(CLAIM_IDP, subjectTokenClaims.issuer)
            .claim(CLAIM_SECURITY_LEVEL, securityLevel)
            .build()
    }

    companion object {
        val signatureAlgorithm: JWSAlgorithm = JWSAlgorithm.HS512
        const val CLAIM_IDP = "idp"
        const val CLAIM_IDENTITY = "sub"
        const val CLAIM_SECURITY_LEVEL = "acr"
    }
}

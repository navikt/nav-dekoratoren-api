package no.nav.dekoratoren.api.innloggingsstatus.oidc

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.PlainJWT
import java.time.LocalDateTime
import java.time.ZoneOffset
import no.nav.security.token.support.core.jwt.JwtToken

object JwtTokenObjectMother {
    fun createJwtToken(
        subject: String = "1234",
        level: Int = 3,
        issueTime: LocalDateTime = LocalDateTime.now(),
        expiry: LocalDateTime = issueTime.plusHours(1),
        identityClaim: String = "sub",
        nbf: LocalDateTime? = LocalDateTime.now(),
        pid: String? = null
    ) = plainSubjectToken(subject, level, issueTime, expiry, identityClaim, nbf, pid)
        .serialize()
        .let { JwtToken(it) }

    private fun tokenClaims(
        subject: String,
        acr: String,
        iat: Long,
        exp: Long,
        identityClaim: String,
        nbf: Long?,
        pid: String?
    ) =
        JWTClaimsSet.Builder()
            .claim("exp", exp)
            .claim("ver", "1.0")
            .claim("iss", "http://dummy.com")
            .claim(identityClaim, subject)
            .claim("aud", "audience")
            .claim("acr", acr)
            .claim("nonce", "nonce")
            .claim("iat", iat)
            .claim("auth_time", 1577876400)
            .claim("jti", "dummy-user")
            .apply {
                nbf?.let {
                    this.claim("nbf", nbf)
                }
                pid?.let {
                    this.claim("pid", pid)
                }
            }
            .build()

    private fun plainSubjectToken(
        subject: String,
        level: Int,
        issueTime: LocalDateTime,
        expiry: LocalDateTime,
        identityClaim: String,
        nbf: LocalDateTime?,
        pid: String?
    ) = PlainJWT(
        tokenClaims(
            subject = subject,
            acr = "Level$level",
            iat = issueTime.toEpochSecond(ZoneOffset.UTC),
            exp = expiry.toEpochSecond(ZoneOffset.UTC),
            identityClaim = identityClaim,
            nbf = nbf?.toEpochSecond(ZoneOffset.UTC),
            pid = pid
        )
    )
}
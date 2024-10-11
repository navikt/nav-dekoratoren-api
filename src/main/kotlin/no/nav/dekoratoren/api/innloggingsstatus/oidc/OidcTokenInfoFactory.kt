package no.nav.dekoratoren.api.innloggingsstatus.oidc

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import no.nav.security.token.support.core.jwt.JwtToken

object OidcTokenInfoFactory {
    fun mapOidcTokenInfo(token: JwtToken, identityClaim: String): OidcTokenInfo {
        return OidcTokenInfo(
            subject = getIdent(token, identityClaim),
            authLevel = extractAuthLevel(token),
            issueTime = getTokenIssueLocalDateTime(token),
            expiryTime = getTokenExpiryLocalDateTime(token),
        )
    }

    private fun extractAuthLevel(token: JwtToken): Int {
        return when (token.jwtTokenClaims.getStringClaim("acr")) {
            "Level3", "idporten-loa-substantial" -> 3
            "Level4", "idporten-loa-high" -> 4
            else -> throw IllegalStateException("Innloggingsnivå ble ikke funnet. Dette skal ikke kunne skje.")
        }
    }

    private fun getTokenExpiryLocalDateTime(token: JwtToken): LocalDateTime {
        return token.jwtTokenClaims.get("exp")
            .let { it as Date }
            .toUtcDateTime()
    }

    private fun getTokenIssueLocalDateTime(token: JwtToken): LocalDateTime {
        return token.jwtTokenClaims.get("iat")
            .let { it as Date }
            .toUtcDateTime()
    }

    private fun getIdent(token: JwtToken, identityClaim: String): String {
        return token.jwtTokenClaims.getStringClaim(identityClaim)
    }

    private fun Date.toUtcDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
}

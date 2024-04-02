package no.nav.dekoratoren.api.innloggingsstatus.auth

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthSummary(val authenticated: Boolean, val authLevel: Int?, val oidc: OidcSummary?) {
    companion object {
        fun fromOidcToken(oidcTokenInfo: OidcTokenInfo?): AuthSummary =
            AuthSummary(
                authenticated = oidcTokenInfo != null,
                authLevel = oidcTokenInfo?.authLevel,
                oidc = oidcTokenInfo?.let { OidcSummary(it.authLevel, it.issueTime, it.expiryTime) })
    }

    data class OidcSummary(val authLevel: Int, val issueTime: LocalDateTime, val expiryTime: LocalDateTime)
}
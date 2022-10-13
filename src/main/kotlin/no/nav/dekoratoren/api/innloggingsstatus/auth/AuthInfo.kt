package no.nav.dekoratoren.api.innloggingsstatus.auth

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo

@JsonInclude(JsonInclude.Include.NON_NULL)
data class AuthInfo(val oidcToken: OidcTokenInfo?) {
    val authenticated: Boolean get() = oidcToken != null
    val subject: String? get() = oidcToken?.subject
    val authLevel: Int? get() = oidcToken?.authLevel
    val expiryTime: LocalDateTime? get() = oidcToken?.expiryTime
}
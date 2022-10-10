package no.nav.dekoratoren.api.auth

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY)
class AuthSummary private constructor(authInfo: no.nav.dekoratoren.api.auth.AuthInfo) {
    private val authenticated: Boolean = authInfo.authenticated
    private val authLevel: Int? = authInfo.authLevel
    private val oidc: no.nav.dekoratoren.api.auth.OidcSummary? =
        no.nav.dekoratoren.api.auth.OidcSummary.Companion.fromAuthInfo(authInfo)

    companion object {
        fun fromAuthInfo(authInfo: no.nav.dekoratoren.api.auth.AuthInfo): no.nav.dekoratoren.api.auth.AuthSummary =
            no.nav.dekoratoren.api.auth.AuthSummary(authInfo)
    }
}

private data class OidcSummary(
    val authLevel: Int,
    val issueTime: LocalDateTime,
    val expiryTime: LocalDateTime
) {
    companion object {
        fun fromAuthInfo(authInfo: no.nav.dekoratoren.api.auth.AuthInfo): no.nav.dekoratoren.api.auth.OidcSummary? {
            return authInfo.oidcToken?.let { oidc ->
                no.nav.dekoratoren.api.auth.OidcSummary(
                    oidc.authLevel,
                    oidc.issueTime,
                    oidc.expiryTime
                )
            }
        }
    }
}
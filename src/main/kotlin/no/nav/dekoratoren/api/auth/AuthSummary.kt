package no.nav.dekoratoren.api.auth

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY
import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonAutoDetect(fieldVisibility = ANY)
class AuthSummary private constructor(authInfo: AuthInfo) {
    private val authenticated: Boolean = authInfo.authenticated
    private val authLevel: Int? = authInfo.authLevel
    private val oidc: OidcSummary? =
        OidcSummary.fromAuthInfo(authInfo)

    companion object {
        fun fromAuthInfo(authInfo: AuthInfo): AuthSummary =
            AuthSummary(authInfo)
    }
}

private data class OidcSummary(
    val authLevel: Int,
    val issueTime: LocalDateTime,
    val expiryTime: LocalDateTime
) {
    companion object {
        fun fromAuthInfo(authInfo: AuthInfo): OidcSummary? {
            return authInfo.oidcToken?.let { oidc ->
                OidcSummary(
                    oidc.authLevel,
                    oidc.issueTime,
                    oidc.expiryTime
                )
            }
        }
    }
}
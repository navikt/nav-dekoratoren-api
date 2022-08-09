package no.nav.personbruker.innloggingsstatus.common.metrics

import no.nav.personbruker.innloggingsstatus.auth.AuthInfo
import no.nav.personbruker.innloggingsstatus.common.epochSecondUtc
import no.nav.personbruker.innloggingsstatus.common.getSecondsSinceUtcEpoch
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenInfo

data class OidcMetrics constructor (
    val authenticated: Boolean,
    val authLevel: Int,
    val tokenAgeSeconds: Long,
    val tokenTimeToExpirySeconds: Long
) {
    companion object {
        fun fromAuthInfo(auth: AuthInfo): OidcMetrics {
            return auth.oidcToken?.let { oidcToken ->
                parseOidcInfo(
                    oidcToken
                )
            }?: OidcMetrics(false, -1, -1, -1)
        }

        private fun parseOidcInfo(oidcToken: OidcTokenInfo): OidcMetrics {
            return OidcMetrics(
                authenticated = true,
                authLevel = oidcToken.authLevel,
                tokenAgeSeconds = getTokenAgeSeconds(
                    oidcToken
                ),
                tokenTimeToExpirySeconds = getTokenTimeToExpirySeconds(
                    oidcToken
                )
            )
        }

        private fun getTokenAgeSeconds(oidcToken: OidcTokenInfo): Long {
            return getSecondsSinceUtcEpoch() - oidcToken.issueTime.epochSecondUtc
        }

        private fun getTokenTimeToExpirySeconds(oidcToken: OidcTokenInfo): Long {
            return oidcToken.expiryTime.epochSecondUtc - getSecondsSinceUtcEpoch()
        }
    }
}
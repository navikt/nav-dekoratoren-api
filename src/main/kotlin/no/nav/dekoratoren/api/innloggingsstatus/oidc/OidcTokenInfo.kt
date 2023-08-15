package no.nav.dekoratoren.api.innloggingsstatus.oidc

import no.nav.dekoratoren.api.innloggingsstatus.auth.TokenInfo
import java.time.LocalDateTime

data class OidcTokenInfo(
    override val subject: String,
    override val authLevel: Int,
    override val issueTime: LocalDateTime,
    override val expiryTime: LocalDateTime
): TokenInfo

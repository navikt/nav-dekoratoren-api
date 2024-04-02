package no.nav.dekoratoren.api.innloggingsstatus.oidc

import java.time.LocalDateTime

data class OidcTokenInfo(
    val subject: String,
    val authLevel: Int,
    val issueTime: LocalDateTime,
    val expiryTime: LocalDateTime
)

package no.nav.dekoratoren.api.innloggingsstatus.auth

import java.time.LocalDateTime

interface TokenInfo {
    val subject: String
    val authLevel: Int
    val issueTime: LocalDateTime?
    val expiryTime: LocalDateTime?
}
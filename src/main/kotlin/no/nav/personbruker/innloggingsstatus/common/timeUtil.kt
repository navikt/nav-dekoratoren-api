package no.nav.personbruker.innloggingsstatus.common

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

val LocalDateTime.epochSecondUtc get() = toEpochSecond(ZoneOffset.UTC)

fun Date.toUtcDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))

fun getSecondsSinceUtcEpoch() = Instant.now().epochSecond
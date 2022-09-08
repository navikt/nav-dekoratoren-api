package no.nav.personbruker.innloggingsstatus.common

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun Date.toUtcDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
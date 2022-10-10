package no.nav.dekoratoren.api.common

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

fun Date.toUtcDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
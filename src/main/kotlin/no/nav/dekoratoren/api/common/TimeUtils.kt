package no.nav.dekoratoren.api.common

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

fun Date.toUtcDateTime(): LocalDateTime = LocalDateTime.ofInstant(this.toInstant(), ZoneId.of("UTC"))
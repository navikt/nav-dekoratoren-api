package no.nav.personbruker.innloggingsstatus.config

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonDeserialize {
    val objectMapper = jacksonObjectMapper().apply {
        registerModule(JavaTimeModule())
    }
}
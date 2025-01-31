package no.nav.dekoratoren.api.consent

import java.util.Date

data class Consent(
    val consent: ConsentOptions,
    val userActionTaken: Boolean,
    val meta: ConsentMeta
)

data class ConsentMeta(
    val createdAt: Date,
    val updatedAt: Date,
    val version: Number
)

data class ConsentOptions(
    val analytics: Boolean,
    val surveys: Boolean
)

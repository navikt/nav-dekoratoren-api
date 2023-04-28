package no.nav.dekoratoren.api.innloggingsstatus.pdl.query

import com.fasterxml.jackson.databind.JsonNode

data class PdlResponse(
    val data: PdlData,
    val extensions: PdlExtensions? = null
)

data class PdlData(
    val person: PdlPersonInfo
)

data class PdlExtensions(
    val warnings: List<PdlWarning>? = null,
)

data class PdlWarning(
    val message: String? = null,
    val details: JsonNode? = null, // Kan være både objekt og string
)
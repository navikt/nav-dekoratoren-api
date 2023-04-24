package no.nav.dekoratoren.api.innloggingsstatus.pdl.query

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PdlResponse(
        val data: PdlData
)

data class PdlData (
        val person: PdlPersonInfo
)
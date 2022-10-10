package no.nav.dekoratoren.api.pdl.query

data class PdlPersonInfo (val navn: List<PdlNavn>)

data class PdlNavn(
    val fornavn: String?,
    val mellomnavn: String?,
    val etternavn: String?
)


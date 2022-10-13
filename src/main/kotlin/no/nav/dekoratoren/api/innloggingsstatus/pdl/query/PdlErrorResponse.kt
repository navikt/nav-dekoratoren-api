package no.nav.dekoratoren.api.innloggingsstatus.pdl.query

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class PdlErrorResponse(val errors: List<PdlError>)

@JsonIgnoreProperties(ignoreUnknown = true)
class PdlError(@JsonProperty("message") message: String) {

    val errorType = PDLErrorType.fromMessage(message)
}

enum class PDLErrorType(val message: String = "") {
    NOT_FOUND("Fant ikke person"),
    NOT_AUTHENTICATED("Ikke autentisert"),
    ABAC_ERROR("Ikke tilgang til å se person"),
    UNKNOWN_ERROR;

    companion object {
        fun fromMessage(message: String): PDLErrorType {
            return values()
                    .find { it.message == message }
                    ?: UNKNOWN_ERROR
        }
    }
}
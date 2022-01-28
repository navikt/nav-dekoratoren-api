package no.nav.personbruker.innloggingsstatus.selfissued

import com.fasterxml.jackson.annotation.JsonProperty

sealed class SelfIssuedTokenResponse {
    data class OK(
        @JsonProperty("access_token")
        val token: String,
        @JsonProperty("expires_in")
        val expiresIn: Int,
    ) : SelfIssuedTokenResponse()

    data class Invalid(
        @JsonProperty("error")
        val error: String,
        @JsonProperty("error_description")
        val errorDescription: String,
    ) : SelfIssuedTokenResponse()
}

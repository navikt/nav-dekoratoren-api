package no.nav.personbruker.innloggingsstatus.selfissued

import com.nimbusds.oauth2.sdk.ErrorObject
import no.nav.security.token.support.core.jwt.JwtToken

sealed class SelfIssuedTokenResponse {
    data class OK(val token: JwtToken) : SelfIssuedTokenResponse()
    data class Invalid(val error: ErrorObject) : SelfIssuedTokenResponse()
}

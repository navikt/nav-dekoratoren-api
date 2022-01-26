package no.nav.personbruker.innloggingsstatus.selfissued

import com.nimbusds.oauth2.sdk.OAuth2Error
import io.ktor.application.ApplicationCall
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenInfoFactory
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.security.token.support.core.jwt.JwtToken

class SelfIssuedTokenService(
    private val selfIssuedTokenValidator: SelfIssuedTokenValidator,
    private val selfIssuedTokenIssuer: SelfIssuedTokenIssuer,
    private val oidcTokenValidator: OidcTokenValidator,
    private val environment: Environment,
) {

    // get and map self issued token from well-known cookie, returns null if invalid or not found
    fun getSelfIssuedToken(call: ApplicationCall): OidcTokenInfo? {
        return selfIssuedTokenValidator.getValidToken(call)?.let { token ->
            OidcTokenInfoFactory.mapOidcTokenInfo(token, SelfIssuedTokenIssuer.CLAIM_IDENTITY)
        }
    }

    // exchanges an inbound ID-porten token with a self-issued token containing custom claims based on the input token
    fun exchangeToken(call: ApplicationCall): SelfIssuedTokenResponse {
        val idportenToken: JwtToken? = oidcTokenValidator.getValidToken(call, environment.idportenIssuer)

        if (idportenToken == null) {
            val description = "Authorization header does not contain a valid ID-porten token."
            val error = OAuth2Error.ACCESS_DENIED.setDescription(description)
            return SelfIssuedTokenResponse.Invalid(error)
        }

        val issuedToken = selfIssuedTokenIssuer.issueToken(idportenToken)
        return SelfIssuedTokenResponse.OK(issuedToken)
    }
}

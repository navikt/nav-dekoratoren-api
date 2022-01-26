package no.nav.personbruker.innloggingsstatus.oidc

import io.ktor.application.ApplicationCall
import no.nav.personbruker.innloggingsstatus.config.Environment

class OidcTokenService(private val oidcTokenValidator: OidcTokenValidator,
                       private val environment: Environment) {

    fun getOidcToken(call: ApplicationCall): OidcTokenInfo? {
        return oidcTokenValidator.getValidToken(call, environment.oidcIssuer)?.let { jwtToken ->
            OidcTokenInfoFactory.mapOidcTokenInfo(jwtToken, environment.identityClaim)
        }
    }
}
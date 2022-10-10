package no.nav.dekoratoren.api.oidc

import io.ktor.server.application.ApplicationCall
import no.nav.dekoratoren.api.config.Environment

class OidcTokenService(private val oidcTokenValidator: OidcTokenValidator,
                       private val environment: Environment) {

    fun getOidcToken(call: ApplicationCall): OidcTokenInfo? {
        return oidcTokenValidator.getValidToken(call, environment.oidcIssuer)?.let { jwtToken ->
            OidcTokenInfoFactory.mapOidcTokenInfo(jwtToken, environment.identityClaim)
        }
    }
}
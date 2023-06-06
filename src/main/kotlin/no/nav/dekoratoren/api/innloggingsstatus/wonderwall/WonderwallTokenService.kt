package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import io.ktor.server.application.ApplicationCall
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfoFactory

class WonderwallTokenService(
    private val wonderwallTokenValidator: WonderwallTokenValidator,
    private val environment: Environment,
) {

    // get and map Wonderwall token from Authorization header (sso)
    // returns null if invalid or not found
    fun getToken(call: ApplicationCall): OidcTokenInfo? {
        return wonderwallTokenValidator.getAuthHeaderToken(call)?.let { token ->
            OidcTokenInfoFactory.mapOidcTokenInfo(token, environment.idportenIdentityClaim)
        }
    }
}

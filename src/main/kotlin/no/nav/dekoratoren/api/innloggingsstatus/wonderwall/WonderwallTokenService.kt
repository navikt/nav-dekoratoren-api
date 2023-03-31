package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import com.nimbusds.oauth2.sdk.OAuth2Error
import io.ktor.server.application.ApplicationCall
import java.time.Duration
import java.time.Instant
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfoFactory
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.security.token.support.core.jwt.JwtToken

class WonderwallTokenService(
    private val wonderwallTokenValidator: WonderwallTokenValidator,
    private val selfIssuedTokenIssuer: SelfIssuedTokenIssuer,
    private val oidcTokenValidator: OidcTokenValidator,
    private val environment: Environment,
) {

    // get and map Wonderwall token in this priority:
    // 1. idporten token from Authorization header (sso)
    // 2. self issued token from well-known cookie
    //
    // returns null if both are invalid or not found
    fun getToken(call: ApplicationCall): OidcTokenInfo? {
        val authHeaderToken = wonderwallTokenValidator.getAuthHeaderToken(call)?.let { token ->
            OidcTokenInfoFactory.mapOidcTokenInfo(token, environment.idportenIdentityClaim)
        }

        if (authHeaderToken != null) {
            return authHeaderToken
        }

        return wonderwallTokenValidator.getSelfIssuedToken(call)?.let { token ->
            OidcTokenInfoFactory.mapOidcTokenInfo(token, SelfIssuedTokenIssuer.CLAIM_IDENTITY)
        }
    }

    // exchanges an inbound ID-porten token with a self-issued token containing custom claims based on the input token
    fun exchangeToken(call: ApplicationCall): SelfIssuedTokenResponse {
        val idportenToken: JwtToken? = oidcTokenValidator.getValidToken(call, environment.oidcIssuer)

        if (idportenToken == null) {
            val description = "Authorization header does not contain a valid ID-porten token."
            return SelfIssuedTokenResponse.Invalid(OAuth2Error.ACCESS_DENIED_CODE, description)
        }

        val issuedToken = selfIssuedTokenIssuer.issueToken(idportenToken)
        val expiresInSeconds = Duration.between(
            Instant.now(), issuedToken.jwtTokenClaims.expirationTime.toInstant()
        ).seconds.toInt()
        return SelfIssuedTokenResponse.OK(issuedToken.tokenAsString, expiresInSeconds)
    }
}

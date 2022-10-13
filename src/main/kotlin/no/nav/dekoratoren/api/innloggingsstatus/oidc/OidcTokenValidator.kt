package no.nav.dekoratoren.api.innloggingsstatus.oidc

import io.ktor.server.application.ApplicationCall
import io.ktor.server.config.ApplicationConfig
import no.nav.security.token.support.core.configuration.IssuerProperties
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler
import no.nav.security.token.support.v2.asIssuerProps

class OidcTokenValidator constructor(applicationConfig: ApplicationConfig) {

    private val resourceRetriever: ProxyAwareResourceRetriever = ProxyAwareResourceRetriever()

    private val jwtTokenValidationHandler: JwtTokenValidationHandler

    private val multiIssuerConfiguration: MultiIssuerConfiguration

    init {
        val issuerPropertiesMap: Map<String, IssuerProperties> = applicationConfig.asIssuerProps()

        multiIssuerConfiguration = MultiIssuerConfiguration(issuerPropertiesMap, resourceRetriever)

        jwtTokenValidationHandler = JwtTokenValidationHandler(multiIssuerConfiguration)
    }

    fun getValidToken(call: ApplicationCall, issuer: String): JwtToken? {
        return jwtTokenValidationHandler.getValidatedTokens(
            JwtTokenHttpRequest(call.request.cookies, call.request.headers)
        ).getJwtToken(issuer)
    }
}

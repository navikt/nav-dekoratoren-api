package no.nav.personbruker.innloggingsstatus.oidc

import io.ktor.server.application.ApplicationCall
import io.ktor.server.config.ApplicationConfig
import java.net.URL
import no.nav.security.token.support.core.configuration.IssuerProperties
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler

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


// Hentet fra TokenValidationContextPrincipal i nav-token-support,
// versjon i dette biblioteket støtter ikke ktor 2.x.x
fun ApplicationConfig.asIssuerProps(): Map<String, IssuerProperties> = this.configList("no.nav.security.jwt.issuers")
    .associate { issuerConfig ->
        issuerConfig.property("issuer_name").getString() to IssuerProperties(
            URL(issuerConfig.property("discoveryurl").getString()),
            issuerConfig.property("accepted_audience").getString().split(","),
            issuerConfig.propertyOrNull("cookie_name")?.getString(),
            IssuerProperties.Validation(
                issuerConfig.propertyOrNull("validation.optional_claims")?.getString()?.split(",") ?: emptyList()
            ),
            IssuerProperties.JwksCache(
                issuerConfig.propertyOrNull("jwks_cache.lifespan")?.getString()?.toLong(),
                issuerConfig.propertyOrNull("jwks_cache.refreshtime")?.getString()?.toLong()
            )
        )
    }


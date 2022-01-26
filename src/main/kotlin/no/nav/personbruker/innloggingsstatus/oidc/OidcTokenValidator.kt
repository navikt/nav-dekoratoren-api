package no.nav.personbruker.innloggingsstatus.oidc

import io.ktor.application.ApplicationCall
import io.ktor.config.ApplicationConfig
import no.nav.security.token.support.core.configuration.IssuerProperties
import no.nav.security.token.support.core.configuration.MultiIssuerConfiguration
import no.nav.security.token.support.core.configuration.ProxyAwareResourceRetriever
import no.nav.security.token.support.core.jwt.JwtToken
import no.nav.security.token.support.core.validation.JwtTokenValidationHandler
import java.net.URL

class OidcTokenValidator constructor(applicationConfig: ApplicationConfig) {

    private val resourceRetriever: ProxyAwareResourceRetriever = ProxyAwareResourceRetriever()

    private val jwtTokenValidationHandler: JwtTokenValidationHandler

    private val multiIssuerConfiguration: MultiIssuerConfiguration

    init {
        val issuerPropertiesMap: Map<String, IssuerProperties> =
            applicationConfig.configList("no.nav.security.jwt.issuers")
                .associate { issuerConfig ->
                    val discoveryurl = URL(issuerConfig.property("discoveryurl").getString())
                    val acceptedAudience = issuerConfig.property("accepted_audience").getString().split(",")
                    val cookieName = issuerConfig.propertyOrNull("cookie_name")?.getString()
                    val properties = IssuerProperties(discoveryurl, acceptedAudience, cookieName)

                    issuerConfig.propertyOrNull("optional_claims")?.getString()?.split(",")?.let { optionalClaims ->
                        properties.validation = IssuerProperties.Validation(optionalClaims)
                    }

                    issuerConfig.property("issuer_name").getString() to properties
                }

        multiIssuerConfiguration = MultiIssuerConfiguration(issuerPropertiesMap, resourceRetriever)

        jwtTokenValidationHandler = JwtTokenValidationHandler(multiIssuerConfiguration)
    }

    fun getValidToken(call: ApplicationCall, issuer: String): JwtToken? {
        return jwtTokenValidationHandler.getValidatedTokens(
            JwtTokenHttpRequest(call.request.cookies, call.request.headers)
        ).getJwtToken(issuer)
    }
}
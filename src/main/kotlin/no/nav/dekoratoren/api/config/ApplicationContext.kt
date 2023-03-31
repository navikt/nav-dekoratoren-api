package no.nav.dekoratoren.api.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.config.ApplicationConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.*
import java.util.concurrent.TimeUnit
import no.finn.unleash.strategy.Strategy
import no.nav.common.featuretoggle.UnleashClient
import no.nav.common.featuretoggle.UnleashClientImpl
import no.nav.common.featuretoggle.UnleashUtils
import no.nav.common.utils.EnvironmentUtils
import no.nav.dekoratoren.api.featuretoggles.ByApplicationStrategy
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlConsumer
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlService
import no.nav.dekoratoren.api.innloggingsstatus.wonderwall.SelfIssuedTokenIssuer
import no.nav.dekoratoren.api.innloggingsstatus.wonderwall.WonderwallTokenService
import no.nav.dekoratoren.api.innloggingsstatus.wonderwall.WonderwallTokenValidator
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService
import no.nav.dekoratoren.api.varsel.VarselbjelleConsumer
import no.nav.dekoratoren.api.varsel.VarselbjelleTokenFetcher
import no.nav.tms.token.support.azure.exchange.AzureServiceBuilder

class ApplicationContext(config: ApplicationConfig) {

    val environment = Environment()
    val httpClient = HttpClientBuilder.build()

    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)

    val oidcTokenValidator = OidcTokenValidator(config)
    val oidcValidationService = OidcTokenService(oidcTokenValidator, environment)

    val azureService = AzureServiceBuilder.buildAzureService(
        cachingEnabled = true,
        enableDefaultProxy = false,
    )

    val unleashClient = unleashClient()

    val pdlConsumer = PdlConsumer(httpClient, environment)
    val pdlService = PdlService(pdlConsumer, azureService, environment)

    val subjectNameService = SubjectNameService(pdlService, setupSubjectNameCache(environment))

    val wonderwallTokenValidator = WonderwallTokenValidator(environment)
    val selfIssuedTokenIssuer = SelfIssuedTokenIssuer(environment)
    val wonderwallTokenService =
        WonderwallTokenService(wonderwallTokenValidator, selfIssuedTokenIssuer, oidcTokenValidator, environment)

    val authTokenService =
        AuthTokenService(oidcValidationService, subjectNameService, wonderwallTokenService)

    val varselbjelleTokenFetcher = VarselbjelleTokenFetcher(azureService, environment.varselbjelleApiClientId)
    val varselbjelleConsumer = VarselbjelleConsumer(environment.varselbjelleApiUrl, httpClient, varselbjelleTokenFetcher)

    val selfTests = listOf(pdlConsumer)
}

private fun setupSubjectNameCache(environment: Environment): Cache<String, String> {
    return Caffeine.newBuilder()
        .maximumSize(environment.subjectNameCacheThreshold.toLong())
        .expireAfterWrite(environment.subjectNameCacheExpiryMinutes, TimeUnit.MINUTES)
        .build()
}

private fun unleashClient(): UnleashClient {
    return UnleashClientImpl(
        EnvironmentUtils.getOptionalProperty(UnleashUtils.UNLEASH_URL_ENV_NAME)
            .orElse("https://unleash.nais.io/api/"),
        EnvironmentUtils.getRequiredProperty("NAIS_APP_NAME"),
        Collections.singletonList(ByApplicationStrategy()) as List<Strategy>?
    )
}
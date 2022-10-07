package no.nav.personbruker.innloggingsstatus.config

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
import no.nav.personbruker.innloggingsstatus.auth.AuthTokenService
import no.nav.personbruker.innloggingsstatus.featuretoggles.ByApplicationStrategy
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenService
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.personbruker.innloggingsstatus.pdl.PdlConsumer
import no.nav.personbruker.innloggingsstatus.pdl.PdlService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenIssuer
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenValidator
import no.nav.personbruker.innloggingsstatus.user.SubjectNameService
import no.nav.personbruker.innloggingsstatus.varsel.VarselbjelleConsumer
import no.nav.personbruker.innloggingsstatus.varsel.VarselbjelleTokenFetcher
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

    val selfIssuedTokenValidator = SelfIssuedTokenValidator(environment)
    val selfIssuedTokenIssuer = SelfIssuedTokenIssuer(environment)
    val selfIssuedTokenService =
        SelfIssuedTokenService(selfIssuedTokenValidator, selfIssuedTokenIssuer, oidcTokenValidator, environment)

    val authTokenService =
        AuthTokenService(oidcValidationService, subjectNameService, selfIssuedTokenService)

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
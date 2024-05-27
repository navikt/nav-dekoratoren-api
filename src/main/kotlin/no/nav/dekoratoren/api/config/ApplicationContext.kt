package no.nav.dekoratoren.api.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.config.ApplicationConfig
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import java.util.concurrent.TimeUnit
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlConsumer
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlService
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

    val pdlConsumer = PdlConsumer(httpClient, environment)
    val pdlService = PdlService(pdlConsumer, azureService, environment)

    val subjectNameService = SubjectNameService(pdlService, setupSubjectNameCache(environment))

    val authTokenService = AuthTokenService(oidcValidationService, subjectNameService)

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

package no.nav.personbruker.innloggingsstatus.config

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.ktor.server.config.ApplicationConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import java.util.concurrent.TimeUnit
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.SensuConfig
import no.nav.personbruker.innloggingsstatus.auth.AuthTokenService
import no.nav.personbruker.innloggingsstatus.common.metrics.MetricsCollector
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenService
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.personbruker.innloggingsstatus.pdl.PdlConsumer
import no.nav.personbruker.innloggingsstatus.pdl.PdlService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenIssuer
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenValidator
import no.nav.personbruker.innloggingsstatus.user.SubjectNameService
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

    val metricsReporter = resolveMetricsReporter(environment)
    val metricsCollector = MetricsCollector(metricsReporter)

    val selfIssuedTokenValidator = SelfIssuedTokenValidator(environment)
    val selfIssuedTokenIssuer = SelfIssuedTokenIssuer(environment)
    val selfIssuedTokenService =
        SelfIssuedTokenService(selfIssuedTokenValidator, selfIssuedTokenIssuer, oidcTokenValidator, environment)

    val authTokenService =
        AuthTokenService(oidcValidationService, subjectNameService, selfIssuedTokenService, metricsCollector)

    val selfTests = listOf(pdlConsumer)
}

private fun setupSubjectNameCache(environment: Environment): Cache<String, String> {
    return Caffeine.newBuilder()
        .maximumSize(environment.subjectNameCacheThreshold.toLong())
        .expireAfterWrite(environment.subjectNameCacheExpiryMinutes, TimeUnit.MINUTES)
        .build()
}

private fun resolveMetricsReporter(environment: Environment): MetricsReporter {
    return if (environment.sensuHost == "" || environment.sensuHost == "stub") {
        StubMetricsReporter()
    } else {
        val sensuConfig = SensuConfig(
            applicationName = environment.applicationName,
            hostName = environment.sensuHost,
            hostPort = environment.sensuPort.toInt(),
            clusterName = environment.clusterName,
            namespace = environment.namespace,
            eventsTopLevelName = "personbruker-innloggingsstatus",
            enableEventBatching = environment.sensuBatchingEnabled,
            eventBatchesPerSecond = environment.sensuBatchesPerSecond
        )

        InfluxMetricsReporter(sensuConfig)
    }
}
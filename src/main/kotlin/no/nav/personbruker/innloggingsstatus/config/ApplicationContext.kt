package no.nav.personbruker.innloggingsstatus.config

import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.dittnav.common.metrics.StubMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.InfluxMetricsReporter
import no.nav.personbruker.dittnav.common.metrics.influx.SensuConfig
import no.nav.personbruker.dittnav.common.cache.EvictingCache
import no.nav.personbruker.dittnav.common.cache.EvictingCacheConfig
import no.nav.personbruker.innloggingsstatus.auth.AuthTokenService
import no.nav.personbruker.innloggingsstatus.common.metrics.MetricsCollector
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenService
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenValidator
import no.nav.personbruker.innloggingsstatus.openam.*
import no.nav.personbruker.innloggingsstatus.pdl.PdlConsumer
import no.nav.personbruker.innloggingsstatus.pdl.PdlService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenIssuer
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenValidator
import no.nav.personbruker.innloggingsstatus.sts.CachingStsService
import no.nav.personbruker.innloggingsstatus.sts.NonCachingStsService
import no.nav.personbruker.innloggingsstatus.sts.STSConsumer
import no.nav.personbruker.innloggingsstatus.sts.StsService
import no.nav.personbruker.innloggingsstatus.sts.cache.StsTokenCache
import no.nav.personbruker.innloggingsstatus.user.SubjectNameService

@KtorExperimentalAPI
class ApplicationContext(config: ApplicationConfig) {

    val environment = Environment()

    val httpClient = HttpClientBuilder.build()

    val oidcTokenValidator = OidcTokenValidator(config)
    val oidcValidationService = OidcTokenService(oidcTokenValidator, environment)

    val openAMConsumer = OpenAMConsumer(httpClient, environment)
    val openAMTokenInfoProvider = setupOpenAmTokenInfoProvider(openAMConsumer, environment)
    val openAMValidationService = OpenAMTokenService(openAMTokenInfoProvider)

    val stsConsumer = STSConsumer(httpClient, environment)
    val pdlConsumer = PdlConsumer(httpClient, environment)
    val stsService = resolveStsService(stsConsumer, environment)
    val pdlService = PdlService(pdlConsumer, stsService)

    val subjectNameCache = setupSubjectNameCache(environment)
    val subjectNameService = SubjectNameService(pdlService, subjectNameCache)

    val metricsReporter = resolveMetricsReporter(environment)
    val metricsCollector = MetricsCollector(metricsReporter)

    val selfIssuedTokenValidator = SelfIssuedTokenValidator(environment)
    val selfIssuedTokenIssuer = SelfIssuedTokenIssuer(environment)
    val selfIssuedTokenService = SelfIssuedTokenService(selfIssuedTokenValidator, selfIssuedTokenIssuer, oidcTokenValidator, environment)

    val authTokenService = AuthTokenService(oidcValidationService, openAMValidationService, subjectNameService, selfIssuedTokenService, metricsCollector)

    val selfTests = listOf(openAMConsumer, stsConsumer, pdlConsumer)
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

private fun resolveStsService(stsConsumer: STSConsumer, environment: Environment): StsService {

    return if (environment.stsCacheEnabled) {
        val stsTokenCache = StsTokenCache(stsConsumer, environment)
        CachingStsService(stsTokenCache)
    } else {
        NonCachingStsService(stsConsumer)
    }
}

private fun setupSubjectNameCache(environment: Environment): EvictingCache<String, String> {
    val cacheThreshold = environment.subjectNameCacheThreshold
    val cacheExpiryMinutes = environment.subjectNameCacheExpiryMinutes

    val evictingCacheConfig = EvictingCacheConfig(
        evictionThreshold = cacheThreshold,
        entryLifetimeMinutes = cacheExpiryMinutes
    )

    return EvictingCache(evictingCacheConfig)
}

private fun setupOpenAmTokenInfoProvider(openAMConsumer: OpenAMConsumer, environment: Environment): OpenAMTokenInfoProvider {
    return if (environment.openAmTokenInfoCacheEnabled) {
        val openAmTokenInfoCache = setupOpenAMTokenInfoCache(environment)
        CachingOpenAmTokenInfoProvider(openAMConsumer, openAmTokenInfoCache)
    } else {
        NonCachingOpenAmTokenInfoProvider(openAMConsumer)
    }
}

private fun setupOpenAMTokenInfoCache(environment: Environment): EvictingCache<String, OpenAMTokenInfo> {
    val cacheThreshold = environment.openAmTokenInfoCacheThreshold
    val cacheExpiryMinutes = environment.openAmTokenInfoCacheExpiryMinutes

    val evictingCacheConfig = EvictingCacheConfig(
        evictionThreshold = cacheThreshold,
        entryLifetimeMinutes = cacheExpiryMinutes
    )

    return EvictingCache(evictingCacheConfig)
}

package no.nav.personbruker.innloggingsstatus.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.routing
import no.nav.personbruker.innloggingsstatus.auth.authApi
import no.nav.personbruker.innloggingsstatus.featuretoggles.featureToggles
import no.nav.personbruker.innloggingsstatus.health.healthApi
import no.nav.personbruker.innloggingsstatus.varsel.varselApi

fun Application.mainModule() {

    install(DefaultHeaders)

    val applicationContext = ApplicationContext(this.environment.config)

    val environment = applicationContext.environment

    install(MicrometerMetrics) {
        registry = applicationContext.appMicrometerRegistry
    }

    install(CORS) {
        allowHost(
            host = environment.corsAllowedHost,
            schemes = environment.corsAllowedSchemes,
        )
        allowCredentials = true
        allowHeader(HttpHeaders.ContentType)
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    routing {
        healthApi(applicationContext.selfTests, applicationContext.appMicrometerRegistry)
        featureToggles(applicationContext.unleashClient)
        authApi(applicationContext.authTokenService, applicationContext.selfIssuedTokenService)
        varselApi(applicationContext.authTokenService, applicationContext.varselbjelleConsumer)
    }

    configureShutdownHook(applicationContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    environment.monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

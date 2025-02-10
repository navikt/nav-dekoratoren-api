package no.nav.dekoratoren.api.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.http.HttpHeaders
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopping
import io.ktor.server.application.install
import io.ktor.server.engine.*
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.defaultheaders.DefaultHeaders
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dekoratoren.api.consent.consent
import no.nav.dekoratoren.api.health.healthApi
import no.nav.dekoratoren.api.innloggingsstatus.auth
import no.nav.dekoratoren.api.varsel.varsel

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
        route("/person/nav-dekoratoren-api") {
            healthApi(applicationContext.selfTests, applicationContext.appMicrometerRegistry)
            auth(applicationContext.authTokenService)
            varsel(applicationContext.oidcValidationService, applicationContext.varselbjelleConsumer)
            consent(applicationContext.consentService)
        }

        // Nødvendig for å støtte gamle innloggingsstatus-ingresser
        route("/person/innloggingsstatus") {
            auth(applicationContext.authTokenService)
        }
    }

    configureShutdownHook(applicationContext.httpClient)
}

private fun Application.configureShutdownHook(httpClient: HttpClient) {
    monitor.subscribe(ApplicationStopping) {
        httpClient.close()
    }
}

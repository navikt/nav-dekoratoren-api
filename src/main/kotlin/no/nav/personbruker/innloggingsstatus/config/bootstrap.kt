package no.nav.personbruker.innloggingsstatus.config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpHeaders
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import io.prometheus.client.hotspot.DefaultExports
import no.nav.personbruker.innloggingsstatus.auth.authApi
import no.nav.personbruker.innloggingsstatus.health.healthApi

@KtorExperimentalAPI
fun Application.mainModule() {

    DefaultExports.initialize()

    install(DefaultHeaders)

    val applicationContext = ApplicationContext(this.environment.config)

    val environment = applicationContext.environment

    install(MaskOriginSubDomain) {
        host(
            host = environment.corsAllowedOrigins,
            schemes = environment.corsAllowedSchemes
        )
    }

    install(CORS) {
        host(
            host = environment.corsAllowedOrigins,
            schemes = environment.corsAllowedSchemes,
            subDomains = environment.corsAllowedSubdomains
        )
        allowCredentials = true
        header(HttpHeaders.ContentType)
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    routing {
        healthApi(applicationContext.selfTests)
        authApi(applicationContext.authTokenService)
    }
}
package no.nav.personbruker.innloggingsstatus.health

import io.ktor.http.ContentType
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Routing
import io.ktor.server.routing.get
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Routing.healthApi(
    selfTests: List<SelfTest>,
    collectorRegistry: PrometheusMeterRegistry,
) {

    val pingJsonResponse = """{"ping": "pong"}"""

    get("/internal/isAlive") {
        call.respondText(text = "ALIVE", contentType = ContentType.Text.Plain)
    }

    get("/internal/isReady") {
        call.respondText(text = "READY", contentType = ContentType.Text.Plain)
    }

    get("/internal/ping") {
        call.respondText(pingJsonResponse, ContentType.Application.Json)
    }

    get("/internal/selftest") {
        call.buildSelftestPage(selfTests)
    }

    get("/internal/metrics") {
        call.respond(collectorRegistry.scrape())
    }
}
package no.nav.personbruker.innloggingsstatus.health

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.prometheus.client.CollectorRegistry
import io.prometheus.client.exporter.common.TextFormat

fun Routing.healthApi(
    selfTests: List<SelfTest>,
    collectorRegistry: CollectorRegistry = CollectorRegistry.defaultRegistry
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
        val names = call.request.queryParameters.getAll("name[]")?.toSet() ?: emptySet()
        call.respondTextWriter(ContentType.parse(TextFormat.CONTENT_TYPE_004)) {
            TextFormat.write004(this, collectorRegistry.filteredMetricFamilySamples(names))
        }
    }
}
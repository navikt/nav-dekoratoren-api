package no.nav.dekoratoren.api.consent

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SamtykkeRoute")

fun Route.consent(consentService: ConsentService) {
    post("/consent") {
        try {
            val consent = call.receive<Consent>()
            consentService.sendConsentInfoToMetabase(consent)
        } catch (e: Exception) {
            logger.warn("Klarte ikke lagre samtykkeinformasjon", e)
            call.respond(
                HttpStatusCode.InternalServerError,
                typeInfo = TODO()
            )
        }

    }
}
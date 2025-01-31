package no.nav.dekoratoren.api.consent

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("ConsentRoute")

fun Route.consent(consentService: ConsentService) {
    post("/consent") {
        try {
            val consent = call.receive<Consent>()
            logger.info("Sending consent information: $consent")
            consentService.sendConsentInfoToMetabase(consent)
            call.respond(HttpStatusCode.OK)
        } catch (e: BadRequestException) {
            logger.warn("Feil i input", e)
            call.respond(HttpStatusCode.BadRequest)
        } catch (e: Exception) {
            logger.warn("Klarte ikke lagre samtykkeinformasjon", e)
            call.respond(HttpStatusCode.InternalServerError)
        }

    }
}

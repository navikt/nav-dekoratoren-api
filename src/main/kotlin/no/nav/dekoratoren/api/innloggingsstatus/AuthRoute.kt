package no.nav.dekoratoren.api.innloggingsstatus

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService
import no.nav.dekoratoren.api.innloggingsstatus.auth.UserInfo
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("innloggingsstatusRoute")

fun Route.auth(authService: AuthTokenService) {

    get("/auth") {
        try {
            authService.getAuthenticatedUserInfo(call).let { userInfo ->
                call.respond(HttpStatusCode.OK, userInfo)
            }
        } catch (e: Exception) {
            logger.warn("Feil ved henting av brukers innloggingsinfo", e)
            UserInfo.unauthenticated()
        }

    }

    get("/summary") {
        try {
            authService.getAuthSummary(call).let { authInfo ->
                call.respond(HttpStatusCode.OK, authInfo)
            }
        } catch (e: Exception) {
            logger.warn("Feil ved henting av summary", e)
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

package no.nav.dekoratoren.api.innloggingsstatus

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService

fun Route.authApi(authService: AuthTokenService) {

    get("/auth") {
        authService.getAuthenticatedUserInfo(call).let { userInfo ->
            call.respond(HttpStatusCode.OK, userInfo)
        }
    }

    get("/summary") {
        try {
            authService.getAuthSummary(call).let { authInfo ->
                call.respond(HttpStatusCode.OK, authInfo)
            }
        } catch (exception: Exception) {
            call.respond(HttpStatusCode.InternalServerError)
        }
    }
}

package no.nav.personbruker.innloggingsstatus.auth

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenResponse
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenService

fun Route.authApi(authService: AuthTokenService, selfIssuedTokenService: SelfIssuedTokenService) {

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

    get("/token") {
        when (val response: SelfIssuedTokenResponse = selfIssuedTokenService.exchangeToken(call)) {
            is SelfIssuedTokenResponse.OK -> call.respond(HttpStatusCode.OK, response.token.tokenAsString)
            is SelfIssuedTokenResponse.Invalid -> call.respond(HttpStatusCode.Unauthorized, response.error.description)
        }
    }
}

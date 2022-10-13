package no.nav.dekoratoren.api.varsel

import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService

fun Route.varselApi(authService: AuthTokenService, varselbjelleConsumer: VarselbjelleConsumer) {
    get("/rest/varsel/hentsiste") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val response = varselbjelleConsumer.getVarselSummary(ident, authLevel)

            call.respond(response.status, response.readBytes())
        }
    }

    post("/rest/varsel/erlest/{varselId}") {
        doIfAuthenticated(authService) { ident, _ ->
            val varselId = call.parameters["varselId"]?: ""

            val response = varselbjelleConsumer.postErLest(ident, varselId)

            call.respond(response.status, response.readBytes())
        }
    }

    get("/varsel/proxy/{proxyPath...}") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val path = call.getParametersAsPath("proxyPath")

            val response = varselbjelleConsumer.makeGetProxyCall(path, ident, authLevel)

            if (response.status == HttpStatusCode.NotFound) {
                call.respond(HttpStatusCode.BadRequest, "Endepunkt [$path] fantes ikke hos tms-varselbjelle-api")
            } else {
                call.respond(response.status, response.readBytes())
            }
        }
    }

    post("/varsel/proxy/{proxyPath...}") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val path = call.getParametersAsPath("proxyPath")

            val response = varselbjelleConsumer.makePostProxyCall(path, ident, authLevel)

            if (response.status == HttpStatusCode.NotFound) {
                call.respond(HttpStatusCode.BadRequest, "Endepunkt [$path] fantes ikke hos tms-varselbjelle-api")
            } else {
                call.respond(response.status, response.readBytes())
            }
        }
    }
}

suspend fun PipelineContext<Unit, ApplicationCall>.doIfAuthenticated(
    authService: AuthTokenService,
    block: suspend (String, Int) -> Unit
) {
    val authInfo = authService.fetchAndParseAuthInfo(call)

    if (authInfo.authenticated) {
        block(authInfo.subject!!, authInfo.authLevel!!)
    } else {
        call.respond(HttpStatusCode.Unauthorized)
    }
}

private fun ApplicationCall.getParametersAsPath(pathParam: String): String {
    return parameters.getAll(pathParam)
        ?.joinToString("/")
        ?: ""
}

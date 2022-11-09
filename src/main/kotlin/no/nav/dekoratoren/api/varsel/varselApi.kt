package no.nav.dekoratoren.api.varsel

import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.pipeline.PipelineContext
import no.nav.dekoratoren.api.innloggingsstatus.auth.AuthTokenService

fun Route.varselApi(authService: AuthTokenService, varselbjelleConsumer: VarselbjelleConsumer) {
    get("/rest/varsel/hentsiste") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val response = varselbjelleConsumer.getVarselSummary(ident, authLevel)

            call.respondBytes(response.readBytes(), response.contentType(), response.status)
        }
    }

    post("/rest/varsel/erlest/{varselId}") {
        doIfAuthenticated(authService) { ident, _ ->
            val varselId = call.parameters["varselId"]?: ""

            val response = varselbjelleConsumer.postErLest(ident, varselId)

            call.respondBytes(response.readBytes(), response.contentType(), response.status)
        }
    }

    get("/varsel/proxy/{proxyPath...}") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val path = call.getParametersAsPath("proxyPath")

            val response = varselbjelleConsumer.makeGetProxyCall(path, ident, authLevel)

            if (response.status == HttpStatusCode.NotFound) {
                call.respond(HttpStatusCode.BadRequest, "Endepunkt [$path] fantes ikke hos tms-varselbjelle-api")
            } else {
                call.respondBytes(response.readBytes(), response.contentType(), response.status)
            }
        }
    }

    post("/varsel/proxy/{proxyPath...}") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val path = call.getParametersAsPath("proxyPath")

            val content = call.request.receiveContent()

            val response = varselbjelleConsumer.makePostProxyCall(path, ident, authLevel, content)

            if (response.status == HttpStatusCode.NotFound) {
                call.respond(HttpStatusCode.BadRequest, "Endepunkt [$path] fantes ikke hos tms-varselbjelle-api")
            } else {
                call.respondBytes(response.readBytes(), response.contentType(), response.status)
            }
        }
    }

    post("/varsel/beskjed/done") {
        doIfAuthenticated(authService) { ident, authLevel ->
            val content = call.request.receiveContent()

            varselbjelleConsumer.postBeskjedDoneAsync(ident, authLevel, content)

            call.respond(HttpStatusCode.Accepted)
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

private fun HttpResponse.contentType(default: ContentType = ContentType.Application.Json): ContentType {
    val contentTypeString = headers[HttpHeaders.ContentType]

    return try {
        ContentType.parse(contentTypeString!!)
    } catch (e: Exception) {
        default
    }
}

private suspend fun ApplicationRequest.receiveContent(default: ContentType = ContentType.Text.Plain): RequestContent {

    val rawContent = call.receive<ByteArray>()

    val contentType = try {
        ContentType.parse(headers[HttpHeaders.ContentType]!!)
    } catch (e: Exception) {
        default
    }

    return RequestContent(rawContent, contentType)
}

private fun ApplicationCall.getParametersAsPath(pathParam: String): String {
    return parameters.getAll(pathParam)
        ?.joinToString("/")
        ?: ""
}

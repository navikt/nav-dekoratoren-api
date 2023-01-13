package no.nav.dekoratoren.api.innloggingsstatus.pdl

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.options
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import java.net.URL
import no.nav.dekoratoren.api.common.bearerHeader
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.health.SelfTest
import no.nav.dekoratoren.api.health.ServiceStatus
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlPersonInfo
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlResponse
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.SubjectNameRequest
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.createSubjectNameRequest

private const val CONSUMER_ID = "nav-dekoratoren-api"
private const val GENERELL = "GEN"

class PdlConsumer(private val client: HttpClient, environment: Environment) : SelfTest {
    private val endpoint = environment.pdlApiUrl

    override val externalServiceName: String get() = "PDL-api"

    suspend fun getPersonInfo(ident: String, accessToken: String): PdlPersonInfo {
        val request = createSubjectNameRequest(ident)

        return postPersonQuery(request, accessToken)
    }

    private suspend fun postPersonQuery(request: SubjectNameRequest, accessToken: String): PdlPersonInfo {
        val response: HttpResponse = client.post {
            url(endpoint)
            contentType(ContentType.Application.Json)
            bearerHeader(accessToken)
            header("Nav-Consumer-Id", CONSUMER_ID)
            header("Tema", GENERELL)
            setBody(request)
        }
        return if (response.status.isSuccess()) {
            try {
                response.body<PdlResponse>().data.person
            } catch (e: Exception) {
                throw PdlException("Kunne ikke utlede person fra PDL-respons. Respons: [${response.bodyAsText()}]")
            }
        } else {
            throw PdlException("Feil i kall mot PDL, HTTP response status=[${response.status}], feilmelding=[${response.bodyAsText()}]")
        }
    }

    override suspend fun externalServiceStatus(): ServiceStatus {
        return try {
            val response = getLivenessResponse()
            when (response.status) {
                HttpStatusCode.OK -> ServiceStatus.OK
                else -> ServiceStatus.ERROR
            }
        } catch (e: Exception) {
            ServiceStatus.ERROR
        }
    }

    private suspend fun getLivenessResponse(): HttpResponse {
        return client.options {
            url(URL("$endpoint/graphql"))
        }
    }
}

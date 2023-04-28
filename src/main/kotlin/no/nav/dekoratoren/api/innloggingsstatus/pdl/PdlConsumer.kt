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
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlWarning
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.SubjectNameRequest
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.createSubjectNameRequest
import org.slf4j.LoggerFactory

private const val CONSUMER_ID = "nav-dekoratoren-api"
private const val GENERELL = "GEN"
private const val BEHANDLINGSNUMMER = "B328"

private val logger = LoggerFactory.getLogger(PdlConsumer::class.java)

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
            header("Behandlingsnummer", BEHANDLINGSNUMMER)
            setBody(request)
        }
        if (response.status.isSuccess()) {
            try {
                val responseBody = response.body<PdlResponse>()
                val warnings = responseBody.extensions?.warnings;
                if (!warnings.isNullOrEmpty()) {
                    logWarnings(warnings)
                }
                return responseBody.data.person
            } catch (e: Exception) {
                throw PdlException("Kunne ikke utlede person fra PDL-respons", e)
            }
        } else {
            throw PdlException("Feil i kall mot PDL, HTTP response status=[${response.status}], feilmelding=[${response.bodyAsText()}]")
        }
    }

    private fun logWarnings(warnings: List<PdlWarning>) {
        warnings.forEach {
            try {
                logger.warn("Advarsel fra PDL: ${it.message}. Detaljer: ${it.details}.")
            } catch (e: Exception) {
                logger.warn("Fikk advarsel fra PDL (deserialisering av advarsel feilet)")
            }
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

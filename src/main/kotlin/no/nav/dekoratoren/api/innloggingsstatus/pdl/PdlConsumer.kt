package no.nav.dekoratoren.api.innloggingsstatus.pdl

import io.ktor.client.HttpClient
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
import java.net.URL
import no.nav.dekoratoren.api.common.bearerHeader
import no.nav.dekoratoren.api.common.readObject
import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.config.JsonDeserialize.objectMapper
import no.nav.dekoratoren.api.health.SelfTest
import no.nav.dekoratoren.api.health.ServiceStatus
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PDLErrorType
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlErrorResponse
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlPersonInfo
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlResponse
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.SubjectNameRequest
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.createSubjectNameRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val CONSUMER_ID = "nav-dekoratoren-api"
private const val GENERELL = "GEN"

class PdlConsumer(private val client: HttpClient, environment: Environment): SelfTest {
    private val endpoint = environment.pdlApiUrl

    private val log: Logger = LoggerFactory.getLogger(PdlConsumer::class.java)

    override val externalServiceName: String get() = "PDL-api"

    suspend fun getPersonInfo(ident: String, accessToken: String): PdlPersonInfo {

        val request = createSubjectNameRequest(ident)

        return parsePdlResponse(postPersonQuery(request, accessToken))
    }

    private suspend fun postPersonQuery(request: SubjectNameRequest, accessToken: String): String {
        return try {
            client.post {
                url(endpoint)
                contentType(ContentType.Application.Json)
                bearerHeader(accessToken)
                header("Nav-Consumer-Id", CONSUMER_ID)
                header("Tema", GENERELL)
                setBody(request)
            }.bodyAsText()
        } catch (e: Exception) {
            throw PdlException("Feil ved kontakt mot pdl-api", e)
        }
    }

    private fun parsePdlResponse(json: String): PdlPersonInfo {
        return try {
            val personResponse: PdlResponse = objectMapper.readObject(json)
            personResponse.data.person
        } catch (e: Exception) {
            handleErrorResponse(json)
        }
    }

    private fun handleErrorResponse(json: String): Nothing {
        try {
            val errorResponse: PdlErrorResponse = objectMapper.readObject(json)
            logErrorResponse(errorResponse)
            throwAppropriateException(errorResponse)
        } catch (e: Exception) {
            throw PdlException("Feil ved deserialisering av svar fra pdl. Response-body lengde [${json.length}]", e)
        }
    }

    private fun throwAppropriateException(response: PdlErrorResponse): Nothing {
        val firstError = response.errors.first().errorType

        if (firstError == PDLErrorType.NOT_AUTHENTICATED) {
            throw PdlAuthenticationException("Fikk autentiseringsfeil mot PDL [$firstError]")
        } else {
            throw PdlException("Fikk feil fra pdl med type [$firstError]")
        }
    }

    private fun logErrorResponse(response: PdlErrorResponse) {
        when (response.errors.first().errorType) {
            PDLErrorType.NOT_FOUND -> log.warn("Fant ikke bruker i PDL.")
            PDLErrorType.NOT_AUTHENTICATED -> log.warn("Autentiseringsfeil mot PDL. Feil i brukertoken eller systemtoken.")
            PDLErrorType.ABAC_ERROR -> log.warn("Systembruker har ikke tilgang til opplysning")
            PDLErrorType.UNKNOWN_ERROR -> log.warn("Ukjent feil mot PDL")
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
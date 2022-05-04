package no.nav.personbruker.innloggingsstatus.sts

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.options
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import java.net.URI
import java.net.URL
import no.nav.personbruker.innloggingsstatus.common.apiKeyHeader
import no.nav.personbruker.innloggingsstatus.common.basicAuth
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.personbruker.innloggingsstatus.health.SelfTest
import no.nav.personbruker.innloggingsstatus.health.ServiceStatus


class STSConsumer(private val client: HttpClient, environment: Environment): SelfTest {

    private val endpoint = URI(environment.securityTokenServiceUrl)
    private val apiKey = environment.stsApiGWKey
    private val username = environment.serviceUsername
    private val password = environment.servicePassword

    override val externalServiceName: String get() = "Security-Token-Service"

    suspend fun getStsToken(): String {
        return try {
            fetchStsToken().accessToken
        } catch (e: Exception) {
            throw STSException("Feil ved kontakt mot sts-tjeneste", e)
        }
    }

    private suspend fun fetchStsToken(): StsTokenResponse {
        return client.get {
            url(URL("$endpoint/rest/v1/sts/token"))
            parameter("grant_type", "client_credentials")
            parameter("scope", "openid")
            apiKeyHeader(apiKey)
            basicAuth(username, password)
        }.body()
    }

    override suspend fun externalServiceStatus(): ServiceStatus {
        return try {
            when (getLivenessResponse().status) {
                HttpStatusCode.OK -> ServiceStatus.OK
                else -> ServiceStatus.ERROR
            }
        } catch (e: Exception) {
            ServiceStatus.ERROR
        }
    }

    private suspend fun getLivenessResponse(): HttpResponse {
        return client.options {
            url(URL("$endpoint/rest/v1/sts/token"))
            basicAuth(username, password)
            apiKeyHeader(apiKey)
        }
    }
}
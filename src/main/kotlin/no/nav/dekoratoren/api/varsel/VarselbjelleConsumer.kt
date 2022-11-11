package no.nav.dekoratoren.api.varsel

import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory

class VarselbjelleConsumer(
    val varselbjelleUrl: String,
    val httpClient: HttpClient,
    val tokenFetcher: VarselbjelleTokenFetcher
) {
    private val log = LoggerFactory.getLogger(VarselbjelleConsumer::class.java)

    suspend fun getVarselSummary(ident: String, authLevel: Int): HttpResponse {
        val accessToken = tokenFetcher.fetchToken()

        return httpClient.request {
            url("$varselbjelleUrl/varsel/sammendrag")
            method = HttpMethod.Get

            header("fodselsnummer", ident)
            header("auth_level", authLevel)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun postErLest(ident: String, varselId: String): HttpResponse {
        val accessToken = tokenFetcher.fetchToken()

        return httpClient.request {
            url("$varselbjelleUrl/varsel/erlest/$varselId")
            method = HttpMethod.Post

            header("fodselsnummer", ident)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun postBeskjedDoneAsync(ident: String, authLevel: Int, content: RequestContent) = launchIO {
        val accessToken = tokenFetcher.fetchToken()

        httpClient.request {
            url("$varselbjelleUrl/varsel/beskjed/done")
            method = HttpMethod.Post

            setBody(content.rawContent)

            header(HttpHeaders.ContentType, content.contentType)
            header("fodselsnummer", ident)
            header("auth_level", authLevel)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun makeGetProxyCall(path: String, ident: String, authLevel: Int): HttpResponse {
        val accessToken = tokenFetcher.fetchToken()

        return httpClient.request {
            url("$varselbjelleUrl/$path")
            method = HttpMethod.Get

            header("fodselsnummer", ident)
            header("auth_level", authLevel)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    suspend fun makePostProxyCall(path: String, ident: String, authLevel: Int, content: RequestContent): HttpResponse {
        val accessToken = tokenFetcher.fetchToken()

        return httpClient.request {
            url("$varselbjelleUrl/$path")
            method = HttpMethod.Post

            setBody(content.rawContent)

            header(HttpHeaders.ContentType, content.contentType)
            header("fodselsnummer", ident)
            header("auth_level", authLevel)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }

    private val IOScope = CoroutineScope(Dispatchers.IO)

    private suspend fun launchIO(block: suspend () -> Unit) = IOScope.launch {
        try {
            block()
        } catch (e: Exception) {
            log.warn("Feil ved håndtering av async-kall.", e)
        }
    }
}

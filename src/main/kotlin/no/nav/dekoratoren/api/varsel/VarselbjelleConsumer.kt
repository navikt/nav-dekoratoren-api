package no.nav.dekoratoren.api.varsel

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.url
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

class VarselbjelleConsumer(
    val varselbjelleUrl: String,
    val httpClient: HttpClient,
    val tokenFetcher: VarselbjelleTokenFetcher
) {
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

    suspend fun makePostProxyCall(path: String, ident: String, authLevel: Int): HttpResponse {
        val accessToken = tokenFetcher.fetchToken()

        return httpClient.request {
            url("$varselbjelleUrl/$path")
            method = HttpMethod.Post

            header("fodselsnummer", ident)
            header("auth_level", authLevel)
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
    }
}

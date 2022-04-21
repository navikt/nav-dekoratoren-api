package no.nav.personbruker.innloggingsstatus.common

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

fun HttpRequestBuilder.basicAuth(username: String, password: String) {
    val encodedCredentials = "$username:$password".toBase64()
    header(HttpHeaders.Authorization, "Basic $encodedCredentials")
}

fun HttpRequestBuilder.bearerHeader(token: String, headerKey: String = HttpHeaders.Authorization) {
    header(headerKey, "Bearer $token")
}

fun HttpRequestBuilder.apiKeyHeader(apiKey: String) {
    header("x-nav-apiKey", apiKey)
}
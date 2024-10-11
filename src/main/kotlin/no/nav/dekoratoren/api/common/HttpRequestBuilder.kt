package no.nav.dekoratoren.api.common

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

fun HttpRequestBuilder.bearerHeader(token: String, headerKey: String = HttpHeaders.Authorization) {
    header(headerKey, "Bearer $token")
}
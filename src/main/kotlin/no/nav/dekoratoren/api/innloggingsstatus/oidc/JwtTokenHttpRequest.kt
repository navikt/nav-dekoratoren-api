package no.nav.dekoratoren.api.innloggingsstatus.oidc

import io.ktor.http.Headers
import io.ktor.server.request.RequestCookies
import no.nav.security.token.support.core.http.HttpRequest

data class JwtTokenHttpRequest(private val cookies: RequestCookies, private val headers: Headers) : HttpRequest {
    override fun getHeader(headerName: String) = headers[headerName]
}
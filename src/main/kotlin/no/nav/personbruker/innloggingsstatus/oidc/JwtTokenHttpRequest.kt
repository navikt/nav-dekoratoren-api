package no.nav.personbruker.innloggingsstatus.oidc

import io.ktor.http.CookieEncoding
import io.ktor.http.Headers
import io.ktor.http.decodeCookieValue
import io.ktor.request.RequestCookies
import no.nav.security.token.support.core.http.HttpRequest

data class JwtTokenHttpRequest(private val cookies: RequestCookies, private val headers: Headers): HttpRequest {

    override fun getCookies() =
        cookies.rawCookies.map {
            NameValueCookie(
                it.key,
                decodeCookieValue(it.value, CookieEncoding.URI_ENCODING)
            )
        }.toTypedArray()

    override fun getHeader(name: String) = headers[name]
}
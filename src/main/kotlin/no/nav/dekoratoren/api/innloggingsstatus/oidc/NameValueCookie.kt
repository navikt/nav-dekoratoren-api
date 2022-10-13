package no.nav.dekoratoren.api.innloggingsstatus.oidc

import no.nav.security.token.support.core.http.HttpRequest

data class NameValueCookie(@JvmField val name: String, @JvmField val value: String): HttpRequest.NameValue {
    override fun getName(): String = name
    override fun getValue(): String = value
}
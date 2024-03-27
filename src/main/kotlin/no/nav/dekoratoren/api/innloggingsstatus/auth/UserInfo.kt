package no.nav.dekoratoren.api.innloggingsstatus.auth

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class UserInfo(
    val authenticated: Boolean,
    val name: String?,
    val securityLevel: String?
) {
    companion object {
        fun authenticated(name: String, authLevel: Int): UserInfo = UserInfo(true, name, authLevel.toString())
        fun unauthenticated(): UserInfo = UserInfo(false, null, null)
    }
}

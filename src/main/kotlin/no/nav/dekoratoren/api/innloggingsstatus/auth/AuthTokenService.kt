package no.nav.dekoratoren.api.innloggingsstatus.auth

import io.ktor.server.application.ApplicationCall
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService

class AuthTokenService(
    private val oidcTokenService: OidcTokenService,
    private val subjectNameService: SubjectNameService,
) {
    suspend fun getAuthenticatedUserInfo(call: ApplicationCall): UserInfo {
        val oidcToken = oidcTokenService.getOidcToken(call)
        return getUserInfo(oidcToken)
    }

    fun getAuthSummary(call: ApplicationCall): AuthSummary {
        val oidcToken = oidcTokenService.getOidcToken(call)
        return AuthSummary.fromOidcToken(oidcToken)
    }

    private suspend fun getUserInfo(oidcTokenInfo: OidcTokenInfo?): UserInfo {
        return if (oidcTokenInfo != null) {
            val subjectName = subjectNameService.getSubjectName(oidcTokenInfo.subject)

            UserInfo.authenticated(subjectName, oidcTokenInfo.authLevel, oidcTokenInfo.subject)
        } else {
            UserInfo.unauthenticated()
        }
    }
}

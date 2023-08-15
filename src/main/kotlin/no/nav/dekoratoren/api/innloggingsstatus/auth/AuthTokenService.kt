package no.nav.dekoratoren.api.innloggingsstatus.auth

import io.ktor.server.application.ApplicationCall
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AuthTokenService(
    private val oidcTokenService: OidcTokenService,
    private val subjectNameService: SubjectNameService,
) {
    private val log: Logger = LoggerFactory.getLogger(AuthTokenService::class.java)

    suspend fun getAuthenticatedUserInfo(call: ApplicationCall): UserInfo {
        return try {
            fetchAndParseAuthenticatedUserInfo(call)
        } catch (e: Exception) {
            log.warn("Feil ved henting av brukers innloggingsinfo", e)
            UserInfo.unAuthenticated()
        }
    }

    fun getAuthSummary(call: ApplicationCall): AuthSummary {
        return fetchAndParseAuthInfo(call).let { authInfo ->
            AuthSummary.fromAuthInfo(authInfo)
        }
    }

    private suspend fun fetchAndParseAuthenticatedUserInfo(call: ApplicationCall): UserInfo {
        val authInfo = fetchAndParseAuthInfo(call)
        return getUserInfo(authInfo)
    }

    fun fetchAndParseAuthInfo(call: ApplicationCall): AuthInfo {
        val oidcToken = oidcTokenService.getOidcToken(call)
        return AuthInfo(oidcToken)
    }

    private suspend fun getUserInfo(authInfo: AuthInfo): UserInfo {
        return if (authInfo.subject != null) {
            val subjectName = subjectNameService.getSubjectName(authInfo.subject!!)
            UserInfo.Companion.authenticated(subjectName, authInfo.authLevel!!)
        } else {
            UserInfo.unAuthenticated()
        }
    }
}

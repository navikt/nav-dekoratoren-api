package no.nav.personbruker.innloggingsstatus.auth

import io.ktor.application.ApplicationCall
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import no.nav.personbruker.innloggingsstatus.common.metrics.MetricsCollector
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenService
import no.nav.personbruker.innloggingsstatus.selfissued.SelfIssuedTokenService
import no.nav.personbruker.innloggingsstatus.user.SubjectNameService
import org.slf4j.LoggerFactory

class AuthTokenService(
    private val oidcTokenService: OidcTokenService,
    private val subjectNameService: SubjectNameService,
    private val selfIssuedTokenService: SelfIssuedTokenService,
    private val metricsCollector: MetricsCollector
) {

    val log = LoggerFactory.getLogger(AuthTokenService::class.java)

    suspend fun getAuthenticatedUserInfo(call: ApplicationCall): UserInfo {
        return try {
            fetchAndParseAuthenticatedUserInfo(call)
        } catch (e: Exception) {
            log.warn("Feil ved henting av brukers innloggingsinfo", e)
            UserInfo.unAuthenticated()
        }
    }

    suspend fun getAuthSummary(call: ApplicationCall): AuthSummary {
        return fetchAndParseAuthInfo(call).let { authInfo ->
            AuthSummary.fromAuthInfo(authInfo)
        }
    }

    private suspend fun fetchAndParseAuthenticatedUserInfo(call: ApplicationCall): UserInfo = coroutineScope {
        val authInfo = fetchAndParseAuthInfo(call)

        val userInfo = getUserInfo(authInfo)

        metricsCollector.recordAuthMetrics(authInfo, userInfo, call)

        userInfo
    }

    private suspend fun fetchAndParseAuthInfo(call: ApplicationCall): AuthInfo = coroutineScope {
        val oidcToken = async { getNewestOidcToken(call) }

        AuthInfo(oidcToken.await())
    }

    private suspend fun getUserInfo(authInfo: AuthInfo): UserInfo {
        return if (authInfo.subject != null) {
            val subjectName = subjectNameService.getSubjectName(authInfo.subject!!)
            UserInfo.authenticated(subjectName, authInfo.authLevel!!)
        } else {
            UserInfo.unAuthenticated()
        }
    }

    private fun getNewestOidcToken(call: ApplicationCall): OidcTokenInfo? {
        val oidcToken = oidcTokenService.getOidcToken(call)
        val selfIssuedToken = selfIssuedTokenService.getSelfIssuedToken(call)

        return when {
            oidcToken == null && selfIssuedToken != null -> selfIssuedToken
            oidcToken != null && selfIssuedToken == null -> oidcToken
            oidcToken != null && selfIssuedToken != null -> oidcToken.mostRecentlyIssued(selfIssuedToken)
            else -> null
        }
    }
}
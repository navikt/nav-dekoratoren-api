package no.nav.personbruker.innloggingsstatus.common.metrics

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpHeaders
import no.nav.personbruker.dittnav.common.metrics.MetricsReporter
import no.nav.personbruker.innloggingsstatus.auth.AuthInfo
import no.nav.personbruker.innloggingsstatus.auth.UserInfo
import no.nav.personbruker.innloggingsstatus.common.UrlPartUtil
import no.nav.personbruker.innloggingsstatus.common.metrics.influx.USER_AUTH_INFO
import no.nav.personbruker.innloggingsstatus.common.metrics.prometheus.PrometheusMetricsCollector

class MetricsCollector(private val metricsReporter: MetricsReporter) {

    suspend fun recordAuthMetrics(authInfo: AuthInfo, userInfo: UserInfo, call: ApplicationCall) {
        buildMetricsAggregate(authInfo, userInfo, call).let { metrics ->
            reportMetrics(metrics)
            PrometheusMetricsCollector.registerAuthMetrics(metrics)
        }
    }

    private fun buildMetricsAggregate(authInfo: AuthInfo, userInfo: UserInfo, call: ApplicationCall): MetricsAggregate {
        return MetricsAggregate(
            authenticated = authInfo.authenticated,
            foundSubjectName = userInfo.name != null,
            operatingAuthLevel = authInfo.authLevel ?: -1,
            oidcMetrics = OidcMetrics.fromAuthInfo(authInfo),
            requestDomain = getRequestDomain(call),
        )
    }

    private suspend fun reportMetrics(metrics: MetricsAggregate) {
        val fieldMap = listOf(
            "foundSubjectName" to metrics.foundSubjectName,
            "operatingAuthLevel" to metrics.operatingAuthLevel,
            "authenticatedWithOidc" to metrics.oidcMetrics.authenticated,
            "oidcAuthLevel" to metrics.oidcMetrics.authLevel,
            "oidcTokenAgeSeconds" to metrics.oidcMetrics.tokenAgeSeconds,
            "oidcTokenTimeToExpirySeconds" to metrics.oidcMetrics.tokenTimeToExpirySeconds,
        ).toMap()

        val tagMap = listOf(
            "authenticationState" to metrics.authenticationState.name,
            "requestDomain" to metrics.requestDomain
        ).toMap()

        metricsReporter.registerDataPoint(USER_AUTH_INFO, fieldMap, tagMap)
    }

    private fun getRequestDomain(call: ApplicationCall): String {
        return  call.request.headers[HttpHeaders.Origin]?.let { UrlPartUtil.parseDomain(it) }
            ?: call.request.headers[HttpHeaders.Referrer]?.let { UrlPartUtil.parseDomain(it) }
            ?: "unknown"
    }
}
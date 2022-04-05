package no.nav.personbruker.innloggingsstatus.common.metrics.prometheus

import io.prometheus.client.Counter
import no.nav.personbruker.innloggingsstatus.common.metrics.AuthenticationState.NONE
import no.nav.personbruker.innloggingsstatus.common.metrics.AuthenticationState.OIDC
import no.nav.personbruker.innloggingsstatus.common.metrics.MetricsAggregate

object PrometheusMetricsCollector {

    private const val NAMESPACE = "personbruker_innloggingsstatus"

    private const val AUTH_REQUESTS_HANDLED_NAME = "auth_requests_handled"
    private const val NOT_AUTHENTICATED_NAME = "auth_not_authenticated"
    private const val AUTHENTICATED_WITH_OIDC_NAME = "auth_authenticated_with_oidc"
    private const val AUTH_LEVEL_OIDC_THREE_NAME = "auth_level_oidc_3"
    private const val AUTH_LEVEL_OIDC_FOUR_NAME = "auth_level_oidc_4"
    private const val AUTH_STEP_UP_OIDC_NAME = "auth_step_up_oidc"
    
    private val AUTH_REQUESTS_HANDLED: Counter = Counter.build()
        .name(AUTH_REQUESTS_HANDLED_NAME)
        .namespace(NAMESPACE)
        .help("Number of /auth requests handled")
        .register()
    
    private val NOT_AUTHENTICATED: Counter = Counter.build()
        .name(NOT_AUTHENTICATED_NAME)
        .namespace(NAMESPACE)
        .help("Number of requests with no valid auth provided")
        .register()
    
    private val AUTHENTICATED_WITH_OIDC: Counter = Counter.build()
        .name(AUTHENTICATED_WITH_OIDC_NAME)
        .namespace(NAMESPACE)
        .help("Number of requests authenticated with oidc only")
        .register()

    private val AUTH_LEVEL_OIDC_THREE: Counter = Counter.build()
        .name(AUTH_LEVEL_OIDC_THREE_NAME)
        .namespace(NAMESPACE)
        .help("Number of requests for which there exists an oidc token with auth level 3")
        .register()
    
    private val AUTH_LEVEL_OIDC_FOUR: Counter = Counter.build()
        .name(AUTH_LEVEL_OIDC_FOUR_NAME)
        .namespace(NAMESPACE)
        .help("Number of requests for which there exists an oidc token with auth level 4")
        .register()

    fun registerAuthMetrics(metricsAggregate: MetricsAggregate) {
        AUTH_REQUESTS_HANDLED.inc()
        handleAuthenticationState(metricsAggregate)
        handleAuthLevel(metricsAggregate)
    }

    private fun handleAuthenticationState(metricsAggregate: MetricsAggregate) {
        when (metricsAggregate.authenticationState) {
            NONE -> NOT_AUTHENTICATED.inc()
            OIDC -> AUTHENTICATED_WITH_OIDC.inc()
        }
    }

    private fun handleAuthLevel(metricsAggregate: MetricsAggregate) {
        when (metricsAggregate.oidcMetrics.authLevel) {
            3 -> AUTH_LEVEL_OIDC_THREE.inc()
            4 -> AUTH_LEVEL_OIDC_FOUR.inc()
        }
    }
}
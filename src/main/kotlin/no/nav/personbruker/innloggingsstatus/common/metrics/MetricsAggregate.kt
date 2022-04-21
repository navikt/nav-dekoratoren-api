package no.nav.personbruker.innloggingsstatus.common.metrics

data class MetricsAggregate (
    val authenticated: Boolean,
    val foundSubjectName: Boolean,
    val operatingAuthLevel: Int,
    val oidcMetrics: OidcMetrics,
    val requestDomain: String
) {
    val authenticationState get() =
        if (oidcMetrics.authenticated) {
            AuthenticationState.OIDC
        } else {
            AuthenticationState.NONE
        }
}

enum class AuthenticationState {
    NONE, OIDC
}
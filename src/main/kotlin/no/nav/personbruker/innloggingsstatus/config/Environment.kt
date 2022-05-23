package no.nav.personbruker.innloggingsstatus.config

import no.nav.personbruker.dittnav.common.util.config.BooleanEnvVar.getEnvVarAsBoolean
import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.LongEnvVar.getEnvVarAsLong
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList

data class Environment(
    val applicationName: String = "innloggingsstatus",
    val identityClaim: String = getEnvVar("OIDC_CLAIM_CONTAINING_THE_IDENTITY", "pid"),
    val oidcIssuer: String = getEnvVar("OIDC_ISSUER"),
    val pdlApiUrl: String = getEnvVar("PDL_API_URL"),
    val pdlAppName: String = getEnvVar("PDL_APP_NAME"),
    val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
    val namespace: String = getEnvVar("NAIS_NAMESPACE"),
    val sensuHost: String = getEnvVar("SENSU_HOST"),
    val sensuPort: String = getEnvVar("SENSU_PORT"),
    val sensuBatchingEnabled: Boolean = getEnvVarAsBoolean("SENSU_BATCHING_ENABLED", true),
    val sensuBatchesPerSecond: Int = getEnvVarAsInt("SENSU_BATCHING_ENABLED", 3),
    val corsAllowedHost: String = getEnvVar("CORS_ALLOWED_HOST"),
    val corsAllowedSchemes: List<String> = getEnvVarAsList("CORS_ALLOWED_SCHEMES", listOf("https")),
    val corsAllowedSubdomains: List<String> = getEnvVarAsList("CORS_ALLOWED_SUBDOMAINS", emptyList()),
    val corsAdditionalAllowedOrigins: List<String> = getEnvVarAsList("CORS_ADDITIONAL_ALLOWED_ORIGINS", emptyList()),
    val subjectNameCacheThreshold: Int = getEnvVarAsInt("SUBJECT_NAME_CACHE_THRESHOLD",  4096),
    val subjectNameCacheExpiryMinutes: Long = getEnvVarAsLong("SUBJECT_NAME_CACHE_EXPIRY_MINUTES", 30),
    val selfIssuedIssuer: String = getEnvVar("SELF_ISSUED_ISSUER"),
    val selfIssuedSecretKey: String = getEnvVar("SELF_ISSUED_SECRET_KEY"),
    val selfIssuedCookieName: String = getEnvVar("SELF_ISSUED_COOKIE_NAME", "innloggingsstatus-token"),
    val idportenIdentityClaim: String = getEnvVar("IDPORTEN_IDENTITY_CLAIM", "pid")
)

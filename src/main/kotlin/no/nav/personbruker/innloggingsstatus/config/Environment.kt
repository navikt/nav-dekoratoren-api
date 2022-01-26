package no.nav.personbruker.innloggingsstatus.config

import no.nav.personbruker.dittnav.common.util.config.BooleanEnvVar.getEnvVarAsBoolean
import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.LongEnvVar.getEnvVarAsLong
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList

data class Environment(
    val applicationName: String = "innloggingsstatus",
    val openAMServiceUrl: String = getEnvVar("OPEN_AM_REST_SERVICE_URL"),
    val identityClaim: String = getEnvVar("OIDC_CLAIM_CONTAINING_THE_IDENTITY", "sub"),
    val oidcIssuer: String = getEnvVar("OIDC_ISSUER"),
    val oidcDiscoveryUrl: String = getEnvVar("LOGINSERVICE_IDPORTEN_DISCOVERY_URL"),
    val oidcAcceptetAudience: String = getEnvVar("LOGINSERVICE_IDPORTEN_AUDIENCE"),
    val securityTokenServiceUrl: String = getEnvVar("SECURITY_TOKEN_SERVICE_URL"),
    val stsApiGWKey: String = getEnvVar("STS_API_GW_KEY"),
    val pdlApiUrl: String = getEnvVar("PDL_API_URL"),
    val pdlApiGWKey: String = getEnvVar("PDL_API_GW_KEY"),
    val serviceUsername: String = getEnvVar("SERVICEUSER_USERNAME"),
    val servicePassword: String = getEnvVar("SERVICEUSER_PASSWORD"),
    val clusterName: String = getEnvVar("NAIS_CLUSTER_NAME"),
    val namespace: String = getEnvVar("NAIS_NAMESPACE"),
    val sensuHost: String = getEnvVar("SENSU_HOST"),
    val sensuPort: String = getEnvVar("SENSU_PORT"),
    val sensuBatchingEnabled: Boolean = getEnvVarAsBoolean("SENSU_BATCHING_ENABLED", true),
    val sensuBatchesPerSecond: Int = getEnvVarAsInt("SENSU_BATCHING_ENABLED", 3),
    val stsCacheEnabled: Boolean = getEnvVarAsBoolean("STS_CACHE_ENABLED"),
    val stsCacheExpiryMarginMinutes: String  = getEnvVar("STS_CACHE_EXPIRY_MARGIN_MINUTES"),
    val corsAllowedHost: String = getEnvVar("CORS_ALLOWED_HOST"),
    val corsAllowedSchemes: List<String> = getEnvVarAsList("CORS_ALLOWED_SCHEMES", listOf("https")),
    val corsAllowedSubdomains: List<String> = getEnvVarAsList("CORS_ALLOWED_SUBDOMAINS", emptyList()),
    val corsAdditionalAllowedOrigins: List<String> = getEnvVarAsList("CORS_ADDITIONAL_ALLOWED_ORIGINS", emptyList()),
    val subjectNameCacheThreshold: Int = getEnvVarAsInt("SUBJECT_NAME_CACHE_THRESHOLD",  4096),
    val subjectNameCacheExpiryMinutes: Long = getEnvVarAsLong("SUBJECT_NAME_CACHE_EXPIRY_MINUTES", 30),
    val openAmTokenInfoCacheEnabled: Boolean = getEnvVarAsBoolean("OPEN_AM_TOKEN_INFO_CACHE_ENABLED", false),
    val openAmTokenInfoCacheThreshold: Int = getEnvVarAsInt("OPEN_AM_TOKEN_INFO_CACHE_THRESHOLD", 1024),
    val openAmTokenInfoCacheExpiryMinutes: Long = getEnvVarAsLong("OPEN_AM_TOKEN_INFO_CACHE_EXPIRY_MINUTES", 1),
    val selfIssuedIssuer: String = getEnvVar("SELF_ISSUED_ISSUER"),
    val selfIssuedSecretKey: String = getEnvVar("SELF_ISSUED_SECRET_KEY"),
    val selfIssuedCookieName: String = getEnvVar("SELF_ISSUED_COOKIE_NAME", "innloggingsstatus-token"),
    val idportenIssuer: String = getEnvVar("IDPORTEN_ISSUER"),
    val idportenAcceptedAudience: String = getEnvVar("IDPORTEN_ACCEPTED_AUDIENCE", "https://nav.no"),
    val idportenWellKnownUrl: String = getEnvVar("IDPORTEN_WELL_KNOWN_URL"),
    val idportenIdentityClaim: String = getEnvVar("IDPORTEN_IDENTITY_CLAIM", "pid")
)

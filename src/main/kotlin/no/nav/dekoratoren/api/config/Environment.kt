package no.nav.dekoratoren.api.config

import no.nav.personbruker.dittnav.common.util.config.IntEnvVar.getEnvVarAsInt
import no.nav.personbruker.dittnav.common.util.config.LongEnvVar.getEnvVarAsLong
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVar
import no.nav.personbruker.dittnav.common.util.config.StringEnvVar.getEnvVarAsList

data class Environment(
    val identityClaim: String = getEnvVar("OIDC_CLAIM_CONTAINING_THE_IDENTITY", "pid"),
    val oidcIssuer: String = getEnvVar("OIDC_ISSUER"),
    val pdlApiUrl: String = getEnvVar("PDL_API_URL"),
    val pdlAppName: String = getEnvVar("PDL_APP_NAME"),
    val corsAllowedHost: String = getEnvVar("CORS_ALLOWED_HOST"),
    val corsAllowedSchemes: List<String> = getEnvVarAsList("CORS_ALLOWED_SCHEMES", listOf("https")),
    val subjectNameCacheThreshold: Int = getEnvVarAsInt("SUBJECT_NAME_CACHE_THRESHOLD", 4096),
    val subjectNameCacheExpiryMinutes: Long = getEnvVarAsLong("SUBJECT_NAME_CACHE_EXPIRY_MINUTES", 30),
    val idportenAudience: String = getEnvVar("IDPORTEN_AUDIENCE"),
    val idportenIdentityClaim: String = getEnvVar("IDPORTEN_IDENTITY_CLAIM", "pid"),
    val idportenIssuer: String = getEnvVar("IDPORTEN_ISSUER"),
    val idportenJwksUri: String = getEnvVar("IDPORTEN_JWKS_URI"),
    val varselbjelleApiUrl: String = getEnvVar("VARSELBJELLE_API_URL"),
    val varselbjelleApiClientId: String = getEnvVar("VARSELBJELLE_API_CLIENT_ID"),
)

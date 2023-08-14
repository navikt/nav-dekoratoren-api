package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.source.JWKSourceBuilder
import com.nimbusds.jose.proc.JWSVerificationKeySelector
import com.nimbusds.jose.proc.SecurityContext
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier
import com.nimbusds.jwt.proc.DefaultJWTProcessor
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.parseAuthorizationHeader
import no.nav.dekoratoren.api.config.Environment
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import java.net.URL

class WonderwallTokenValidator(environment: Environment) {
    private val log = LoggerFactory.getLogger(WonderwallTokenValidator::class.java)

    // ID-porten does not include 'nbf' claim in their JWTs, 'aud' is also an optional claim
    private val requiredClaims = setOf(
        "sub",
        "iss",
        "iat",
        "exp"
    )

    private val issuer = environment.idportenIssuer
    private val jwksUrl = URL(environment.idportenJwksUri)

    private val jwtProcessor = DefaultJWTProcessor<SecurityContext>().apply {
        jwsKeySelector = JWSVerificationKeySelector(
            JWSAlgorithm.RS256,
            JWKSourceBuilder.create<SecurityContext>(jwksUrl)
                .retrying(true)
                .outageTolerant(true)
                .build()
        )
        jwtClaimsSetVerifier = DefaultJWTClaimsVerifier(
            JWTClaimsSet.Builder()
                .issuer(issuer)
                .build(),
            requiredClaims
        )
    }

    private fun validate(token: String): JwtToken {
        jwtProcessor.process(token, null)
        return JwtToken(token)
    }

    fun getAuthHeaderToken(call: ApplicationCall): JwtToken? {
        val authHeader: HttpAuthHeader? = try {
            call.request.parseAuthorizationHeader()
        } catch (e: Exception) {
            log.info("Ugyldig Authorization header: ${e.message}", e)
            null
        }

        if (authHeader is HttpAuthHeader.Single && authHeader.authScheme.lowercase() == "bearer") {
            return try {
                validate(authHeader.blob)
            } catch (e: Exception) {
                log.info("Kunne ikke validere token: ${e.message}", e)
                null
            }
        }

        return null
    }
}

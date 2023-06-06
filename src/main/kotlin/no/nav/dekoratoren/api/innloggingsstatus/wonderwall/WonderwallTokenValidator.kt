package no.nav.dekoratoren.api.innloggingsstatus.wonderwall

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.parseAuthorizationHeader
import no.nav.dekoratoren.api.config.Environment
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory
import java.net.URL

private val JWS_ALGORITHM = JWSAlgorithm.RS256

class WonderwallTokenValidator(environment: Environment) {
    private val log = LoggerFactory.getLogger(WonderwallTokenValidator::class.java)

    private val idportenValidator: IDTokenValidator

    init {
        val idportenIssuer = Issuer(environment.idportenIssuer)
        val idportenClientID = ClientID(environment.idportenAudience)
        val idportenJwksUri = URL(environment.idportenJwksUri)
        idportenValidator = IDTokenValidator(idportenIssuer, idportenClientID, JWS_ALGORITHM, idportenJwksUri)
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
                val signedJwt = SignedJWT.parse(authHeader.blob)
                idportenValidator.validate(signedJwt, null)

                JwtToken(authHeader.blob)
            } catch (e: Exception) {
                log.info("Kunne ikke validere token: ${e.message}", e)
                null
            }
        }

        return null
    }
}

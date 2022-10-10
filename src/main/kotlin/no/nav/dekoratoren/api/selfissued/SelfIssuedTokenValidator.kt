package no.nav.dekoratoren.api.selfissued

import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.auth.Secret
import com.nimbusds.oauth2.sdk.id.ClientID
import com.nimbusds.oauth2.sdk.id.Issuer
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator
import io.ktor.server.application.ApplicationCall
import no.nav.dekoratoren.api.config.Environment
import no.nav.security.token.support.core.jwt.JwtToken
import org.slf4j.LoggerFactory

class SelfIssuedTokenValidator(private val environment: Environment) {
    private val log = LoggerFactory.getLogger(SelfIssuedTokenValidator::class.java)

    private val validator: IDTokenValidator

    init {
        val issuer = Issuer(environment.selfIssuedIssuer)
        val clientID = ClientID(environment.selfIssuedIssuer)
        val secret = Secret(environment.selfIssuedSecretKey)
        validator = IDTokenValidator(issuer, clientID, SelfIssuedTokenIssuer.signatureAlgorithm, secret)
    }

    fun getValidToken(call: ApplicationCall): JwtToken? {
        val cookieName = environment.selfIssuedCookieName
        val cookieValue = call.request.cookies[cookieName]

        if (cookieValue == null) {
            log.debug("Fant ingen cookie med navn $cookieName")
            return null
        }

        return try {
            val signedJwt = SignedJWT.parse(cookieValue)
            validator.validate(signedJwt, null)

            JwtToken(cookieValue)
        } catch (e: Exception) {
            log.info("Kunne ikke validere token: ${e.message}", e)
            null
        }
    }
}

package no.nav.personbruker.innloggingsstatus.sts.jwt

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import java.time.Instant
import no.nav.personbruker.innloggingsstatus.common.fromBase64
import no.nav.personbruker.innloggingsstatus.common.readObject
import no.nav.personbruker.innloggingsstatus.config.JsonDeserialize.objectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object JwtTokenDecoder {

    private val log: Logger = LoggerFactory.getLogger(JwtTokenDecoder::class.java)

    fun decodeExpiryTime(jwtToken: String): Instant {
        return try {
            val payloadString = jwtToken.split(".")
                .let { segments -> segments[1].fromBase64()}

            val payload: JwtPayload = objectMapper.readObject(payloadString)
            Instant.ofEpochSecond(payload.expiryTime)
        } catch (e: JsonMappingException) {
            log.warn("Error parsing expiry date from jwt token.")
            throw Exception("Error parsing expiry date from jwt token.", e)
        } catch (e: JsonParseException) {
            log.warn("Invalid json found in jwt token.")
            throw Exception("Invalid json found in jwt token.", e)
        } catch (e: Exception) {
            log.warn("Exception thrown while parsing jwt expiry time.")
            throw Exception("Exception thrown while parsing jwt expiry time.", e)
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
private class JwtPayload constructor(@JsonProperty(value = "exp") expiryTime: String) {
    val expiryTime = expiryTime.toLong()
}
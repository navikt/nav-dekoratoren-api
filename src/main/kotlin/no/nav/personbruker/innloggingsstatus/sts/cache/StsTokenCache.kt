package no.nav.personbruker.innloggingsstatus.sts.cache

import java.time.Instant
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.personbruker.innloggingsstatus.sts.STSConsumer
import org.slf4j.LoggerFactory

class StsTokenCache (private val stsConsumer: STSConsumer, environment: Environment) {

    private val mutex = Mutex()
    private val log = LoggerFactory.getLogger(StsTokenCache::class.java)

    private val tokenSlot = TokenSlot(environment.stsCacheExpiryMarginMinutes.toLong())

    suspend fun getStsToken(): String {
        return getValidTokenOrNull()
            ?: getRenewedToken()
    }

    suspend fun invalidateToken() {
        mutex.withLock {
            tokenSlot.clear()
        }
    }

    private suspend fun getValidTokenOrNull(): String? {
        return mutex.withLock {
            if (tokenSlot.holdsValidToken()) {
                tokenSlot.get()
            } else {
                null
            }
        }
    }

    private suspend fun getRenewedToken(): String {
        return mutex.withLock {
            if (tokenSlot.holdsValidToken()) {
                tokenSlot.get()
            } else {
                log.info("Renewing cached sts token")
                val token = stsConsumer.getStsToken()
                tokenSlot.set(token)
                token
            }
        }
    }
}

private class TokenSlot(val expiryMargin: Long) {
    private var token: StsTokenWithClaims? = null
    private val log = LoggerFactory.getLogger(TokenSlot::class.java)

    fun clear() {
        log.info("Clearing tokenSlot value")
        token = null
    }

    fun set(newToken: String) {
        log.info("Setting tokenSlot value")
        token = StsTokenWithClaims(newToken)
    }

    fun get(): String {
        log.info("Fetching token from tokenSlot")
        return token?.tokenString ?: throw RuntimeException("Called 'get' on empty token slot")
    }

    fun holdsValidToken(): Boolean {
        return token != null && !tokenIsExpiringOrExpired()
    }

    private fun tokenIsExpiringOrExpired(): Boolean {
        return token?.expiryClaim
            ?.isBefore(currentTimePlusMargin())
            ?: false
    }

    private fun currentTimePlusMargin(): Instant {
        return Instant.now().plus(expiryMargin, ChronoUnit.MINUTES)
    }
}
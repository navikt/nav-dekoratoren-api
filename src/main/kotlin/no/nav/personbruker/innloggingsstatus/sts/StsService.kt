package no.nav.personbruker.innloggingsstatus.sts

import no.nav.personbruker.innloggingsstatus.sts.cache.StsTokenCache
import org.slf4j.LoggerFactory

class StsService(private val stsTokenCache: StsTokenCache) {

    private val log = LoggerFactory.getLogger(StsService::class.java)

    suspend fun getStsToken(): String {
        return stsTokenCache.getStsToken()
    }

    suspend fun invalidateToken() {
        log.info("Invaliderer cachet sts-token.")
        stsTokenCache.invalidateToken()
    }
}
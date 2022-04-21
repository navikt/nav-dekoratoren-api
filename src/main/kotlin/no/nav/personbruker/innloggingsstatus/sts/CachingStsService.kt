package no.nav.personbruker.innloggingsstatus.sts

import no.nav.personbruker.innloggingsstatus.sts.cache.StsTokenCache
import org.slf4j.LoggerFactory

class CachingStsService(private val stsTokenCache: StsTokenCache): StsService {

    private val log = LoggerFactory.getLogger(CachingStsService::class.java)

    override suspend fun getStsToken(): String {
        return stsTokenCache.getStsToken()
    }

    override suspend fun invalidateToken() {
        log.info("Invaliderer cachet sts-token.")
        stsTokenCache.invalidateToken()
    }
}
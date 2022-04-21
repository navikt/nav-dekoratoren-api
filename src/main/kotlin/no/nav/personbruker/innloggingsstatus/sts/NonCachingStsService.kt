package no.nav.personbruker.innloggingsstatus.sts

import org.slf4j.LoggerFactory

class NonCachingStsService (private val stsConsumer: STSConsumer): StsService {

    private val log = LoggerFactory.getLogger(NonCachingStsService::class.java)

    override suspend fun getStsToken(): String {
        return stsConsumer.getStsToken()
    }

    override suspend fun invalidateToken() {
        log.info("invalidateToken() ble kalt på en ikke-cachende sts service.")
    }
}
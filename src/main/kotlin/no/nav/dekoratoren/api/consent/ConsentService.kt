package no.nav.dekoratoren.api.consent

import org.slf4j.LoggerFactory

class ConsentService {
    private val logger = LoggerFactory.getLogger(ConsentService::class.java)

    fun sendConsentInfoToMetabase(consent: Consent) {
        logger.info("Sending consent information: $consent")

    }
}
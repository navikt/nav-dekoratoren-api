package no.nav.dekoratoren.api.pdl

import no.nav.dekoratoren.api.config.Environment
import no.nav.dekoratoren.api.pdl.query.PdlNavn
import no.nav.tms.token.support.azure.exchange.AzureService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdlService(
    private val pdlConsumer: PdlConsumer,
    private val azureService: AzureService,
    private val environment: Environment
) {

    private val log: Logger = LoggerFactory.getLogger(PdlService::class.java)

    suspend fun getSubjectName(ident: String): PdlNavn? {
        return try {
            azureService.getAccessToken(environment.pdlAppName).let { accessToken ->
                pdlConsumer.getPersonInfo(ident, accessToken)
            }.navn.first()
        } catch (e: PdlAuthenticationException) {
            log.warn("Fikk autentiseringsfeil mot pdl.")
            null
        } catch (e: PdlException) {
            log.warn("Fikk feil ved kontakt mot pdl.", e)
            null
        } catch (e: Exception) {
            log.warn("Det oppstod en uventet feil under henting av navn fra pdl.", e)
            null
        }
    }
}
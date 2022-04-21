package no.nav.personbruker.innloggingsstatus.pdl

import no.nav.personbruker.innloggingsstatus.pdl.query.PdlNavn
import no.nav.personbruker.innloggingsstatus.sts.STSException
import no.nav.personbruker.innloggingsstatus.sts.StsService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class PdlService(private val pdlConsumer: PdlConsumer, private val stsService: StsService) {

    private val log: Logger = LoggerFactory.getLogger(PdlService::class.java)

    suspend fun getSubjectName(ident: String): PdlNavn? {
        return try {
            stsService.getStsToken().let { stsToken ->
                pdlConsumer.getPersonInfo(ident, stsToken)
            }.navn.first()
        } catch (e: STSException) {
            log.warn("Klarte ikke hente sts-token for å autentisere mot pdl.", e)
            null
        } catch (e: PdlAuthenticationException) {
            stsService.invalidateToken()
            log.info("Invaliderer sts-token grunnet autentiseringsfeil mot pdl.")
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
package no.nav.personbruker.innloggingsstatus.user

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.innloggingsstatus.pdl.PdlService

class SubjectNameService(private val pdlService: PdlService) {

    private val cache: Cache<String, String> = Caffeine.newBuilder().build()

    suspend fun getSubjectName(subject: String): String {
        return cache.get(subject) {
            runBlocking {
                fetchNameFromPdlAndConcatenate(subject) ?: subject
            }
        }
    }

    private suspend fun fetchNameFromPdlAndConcatenate(subject: String): String? {
        return pdlService.getSubjectName(subject)
            ?.let { pdlNavn -> listOf(pdlNavn.fornavn, pdlNavn.mellomnavn, pdlNavn.etternavn) }
            ?.filter { navn -> !navn.isNullOrBlank() }
            ?.joinToString(" ")
    }

}

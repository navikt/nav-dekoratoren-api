package no.nav.dekoratoren.api.innloggingsstatus.user

import com.github.benmanes.caffeine.cache.Cache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlService

class SubjectNameService(private val pdlService: PdlService, private val cache: Cache<String, String>) {

    suspend fun getSubjectName(subject: String): String = withContext(Dispatchers.IO) {
        cache.get(subject) {
            runBlocking {
                fetchNameFromPdlAndConcatenate(subject) ?: subject
            }
        }
    }

    private suspend fun fetchNameFromPdlAndConcatenate(subject: String): String? {
        return pdlService.getSubjectName(subject)
            ?.let { listOfNotNull(it.fornavn, it.mellomnavn, it.etternavn) }
            ?.filter { it.isNotBlank() }
            ?.joinToString(" ")
    }
}

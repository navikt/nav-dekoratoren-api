package no.nav.personbruker.innloggingsstatus.user

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.dittnav.common.util.cache.EvictingCache
import no.nav.personbruker.innloggingsstatus.cache.MockedEvictingCacheFactory
import no.nav.personbruker.innloggingsstatus.pdl.PdlService
import no.nav.personbruker.innloggingsstatus.pdl.query.PdlNavn
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class SubjectNameServiceTest {

    val nameCache: EvictingCache<String, String> = MockedEvictingCacheFactory.createShallowCache()
    val pdlService: PdlService = mockk()

    val subject = "123465"
    val fornavn = "Fornavn"
    val mellomnavn = "Mellomnavn"
    val etternavn = "Etternavn"

    val subjectNameService = SubjectNameService(pdlService, nameCache)

    @Test
    fun `should return subject if name was not found`() {

        coEvery { pdlService.getSubjectName(subject) } returns null

        val result = runBlocking { subjectNameService.getSubjectName(subject) }

        result `should be equal to` subject
    }

    @Test
    fun `should concatenate full name correctly`() {
        val fullName = PdlNavn(fornavn, mellomnavn, etternavn)

        coEvery { pdlService.getSubjectName(subject) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(subject) }

        result `should be equal to` "$fornavn $mellomnavn $etternavn"
    }

    @Test
    fun `should concatenate name correctly when middle name is missing`() {
        val fullName = PdlNavn(fornavn, null, etternavn)

        coEvery { pdlService.getSubjectName(subject) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(subject) }

        result `should be equal to` "$fornavn $etternavn"
    }

    @Test
    fun `should concatenate name correctly when middle name is an empty string`() {
        val fullName = PdlNavn(fornavn, "", etternavn)

        coEvery { pdlService.getSubjectName(subject) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(subject) }

        result `should be equal to` "$fornavn $etternavn"
    }

}
package no.nav.dekoratoren.api.user

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.pdl.PdlService
import no.nav.dekoratoren.api.pdl.query.PdlNavn
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class SubjectNameServiceTest {

    val pdlService: PdlService = mockk()
    val cache: Cache<String, String> = Caffeine.newBuilder().build()

    val subject = "123465"
    val fornavn = "Fornavn"
    val mellomnavn = "Mellomnavn"
    val etternavn = "Etternavn"

    val subjectNameService = SubjectNameService(pdlService, cache)

    @BeforeEach
    fun setup() {
        cache.invalidateAll()
    }

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

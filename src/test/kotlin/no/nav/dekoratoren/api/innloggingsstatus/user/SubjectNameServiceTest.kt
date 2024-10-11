package no.nav.dekoratoren.api.innloggingsstatus.user

import com.github.benmanes.caffeine.cache.Cache
import com.github.benmanes.caffeine.cache.Caffeine
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.innloggingsstatus.pdl.PdlService
import no.nav.dekoratoren.api.innloggingsstatus.pdl.query.PdlNavn
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SubjectNameServiceTest {

    val pdlService: PdlService = mockk()
    val cache: Cache<String, String> = Caffeine.newBuilder().build()

    val subjectNameService = SubjectNameService(pdlService, cache)

    @BeforeEach
    fun setup() {
        cache.invalidateAll()
    }

    @Test
    fun `should return subject if name was not found`() {

        coEvery { pdlService.getSubjectName(SUBJECT) } returns null

        val result = runBlocking { subjectNameService.getSubjectName(SUBJECT) }

        result shouldBe SUBJECT
    }

    @Test
    fun `should concatenate full name correctly`() {
        val fullName = PdlNavn(FORNAVN, MELLOMNAVN, ETTERNAVN)

        coEvery { pdlService.getSubjectName(SUBJECT) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(SUBJECT) }

        result shouldBe "$FORNAVN $MELLOMNAVN $ETTERNAVN"
    }

    @Test
    fun `should concatenate name correctly when middle name is missing`() {
        val fullName = PdlNavn(FORNAVN, null, ETTERNAVN)

        coEvery { pdlService.getSubjectName(SUBJECT) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(SUBJECT) }

        result shouldBe "$FORNAVN $ETTERNAVN"
    }

    @Test
    fun `should concatenate name correctly when middle name is an empty string`() {
        val fullName = PdlNavn(FORNAVN, "", ETTERNAVN)

        coEvery { pdlService.getSubjectName(SUBJECT) } returns fullName

        val result = runBlocking { subjectNameService.getSubjectName(SUBJECT) }

        result shouldBe "$FORNAVN $ETTERNAVN"
    }

    companion object {
        val SUBJECT = "123465"
        val FORNAVN = "Fornavn"
        val MELLOMNAVN = "Mellomnavn"
        val ETTERNAVN = "Etternavn"
    }
}

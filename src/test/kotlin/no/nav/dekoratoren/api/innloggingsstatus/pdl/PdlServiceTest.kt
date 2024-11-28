package no.nav.dekoratoren.api.innloggingsstatus.pdl

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.config.Environment
import no.nav.tms.token.support.azure.exchange.AzureService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PdlServiceTest {

    val azureService: AzureService = mockk()
    val pdlConsumer: PdlConsumer = mockk()
    val environment: Environment = mockk()

    val pdlService = PdlService(pdlConsumer, azureService, environment)

    @BeforeEach
    fun setupCommonMocks() {
        coEvery { environment.pdlAppName } returns APP_NAME
        coEvery { azureService.getAccessToken(APP_NAME) } returns ACCESS_TOKEN
    }

    @Test
    fun `should map valid response correctly`() {
        val pdlResponse = PdlPersonInfoObjectMother.createPdlPersonInfo(FORNAVN, MELLOMNAVN, ETTERNAVN)
        coEvery { pdlConsumer.getPersonInfo(IDENT, ACCESS_TOKEN) } returns pdlResponse

        val response = requireNotNull(runBlocking { pdlService.getSubjectName(IDENT) })

        assertSoftly(response) {
            fornavn shouldBe FORNAVN
            mellomnavn shouldBe MELLOMNAVN
            etternavn shouldBe ETTERNAVN
        }
    }

    @Test
    fun `should return null if we received an error response from pdl due to a bad token`() {
        coEvery { pdlConsumer.getPersonInfo(IDENT, ACCESS_TOKEN) } throws PdlException()

        val response = runBlocking { pdlService.getSubjectName(IDENT) }

        response.shouldBeNull()
    }

    companion object {
        val APP_NAME = "appName"

        val IDENT = "123456"
        val FORNAVN = "Fornavn"
        val MELLOMNAVN = "Mellomnavn"
        val ETTERNAVN = "Etternavn"

        val ACCESS_TOKEN = "accessToken"
    }
}
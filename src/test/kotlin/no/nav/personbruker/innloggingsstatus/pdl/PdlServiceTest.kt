package no.nav.personbruker.innloggingsstatus.pdl

import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.innloggingsstatus.config.Environment
import no.nav.tms.token.support.azure.exchange.AzureService
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class PdlServiceTest {

    val azureService: AzureService = mockk()
    val pdlConsumer: PdlConsumer = mockk()
    val environment: Environment = mockk()

    val appName = "appName"

    val ident = "123456"
    val fornavn = "Fornavn"
    val mellomnavn = "Mellomnavn"
    val etternavn = "Etternavn"

    val accessToken = "accessToken"

    val pdlService = PdlService(pdlConsumer, azureService, environment)

    @Test
    fun `should return null if we received an error response from pdl due to a bad token`() {
        coEvery { environment.pdlAppName } returns appName
        coEvery { azureService.getAccessToken(appName) } returns accessToken
        coEvery { pdlConsumer.getPersonInfo(ident, accessToken) } throws PdlAuthenticationException()

        val response = runBlocking { pdlService.getSubjectName(ident) }

        response `should be equal to` null
    }

    @Test
    fun `should just return null if we did not receive a valid response for any other reason`() {
        coEvery { environment.pdlAppName } returns appName
        coEvery { azureService.getAccessToken(appName) } returns accessToken
        coEvery { pdlConsumer.getPersonInfo(ident, accessToken) } throws PdlException()

        val response = runBlocking { pdlService.getSubjectName(ident) }

        response `should be equal to` null
    }

    @Test
    fun `should map valid response correctly`() {
        val pdlResponse = PdlPersonInfoObjectMother.createPdlPersonInfo(fornavn, mellomnavn, etternavn)

        coEvery { environment.pdlAppName } returns appName
        coEvery { azureService.getAccessToken(appName) } returns accessToken
        coEvery { pdlConsumer.getPersonInfo(ident, accessToken) } returns pdlResponse

        val response = runBlocking { pdlService.getSubjectName(ident) }

        response?.fornavn `should be equal to` fornavn
        response?.mellomnavn `should be equal to` mellomnavn
        response?.etternavn `should be equal to` etternavn
    }
}
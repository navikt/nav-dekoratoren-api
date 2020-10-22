package no.nav.personbruker.innloggingsstatus.openam

import io.ktor.application.ApplicationCall
import io.mockk.*
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test


internal class OpenAMTokenServiceTest {

    private val call: ApplicationCall = mockk()

    private val subject = "123456"
    private val authLevel = 4
    private val essoToken = "token"

    private val tokenProvider: OpenAMTokenInfoProvider = mockk()
    private val openAMTokenService = OpenAMTokenService(tokenProvider)

    @AfterEach
    fun cleanUp() {
        clearMocks(call)
    }

    @Test
    fun `should not attempt to fetch token info if nav-esso cookie is missing`() {
        every { call.request.cookies[any()] } returns null

        val response = runBlocking { openAMTokenService.getOpenAMToken(call) }

        response `should be equal to` null

        coVerify(exactly = 0) { tokenProvider.getTokenInfo(any()) }
    }

    @Test
    fun `should query for token info if nav-esso cookie is found`() {
        every { call.request.cookies["nav-esso"] } returns essoToken
        coEvery { tokenProvider.getTokenInfo(essoToken) } returns OpenAMTokenInfo(subject, authLevel)

        val response = runBlocking { openAMTokenService.getOpenAMToken(call) }

        response?.authLevel `should be equal to` authLevel
        response?.subject `should be equal to` subject
    }
}
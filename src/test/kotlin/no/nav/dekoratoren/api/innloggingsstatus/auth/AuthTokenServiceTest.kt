package no.nav.dekoratoren.api.innloggingsstatus.auth

import io.ktor.server.application.ApplicationCall
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class AuthTokenServiceTest {

    private val subject1 = "123"
    private val subject2 = "456"

    private val subject1Name = "oneTwoThree"
    private val subject2Name = "fourFiveSix"

    private val oidcTokenService: OidcTokenService = mockk()
    private val subjectNameService: SubjectNameService = mockk()

    private val authTokenService = AuthTokenService(
        oidcTokenService,
        subjectNameService,
    )

    private val call: ApplicationCall = mockk()

    @Test
    fun `should provide correct info for oidc token`() {
        val tokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns tokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "3"
    }

    @Test
    fun `should provide correct info when unauthenticated`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(any()) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` false
        subjectInfo.name `should be equal to` null
        subjectInfo.securityLevel `should be equal to` null
    }

    @Test
    fun `should defer to security level provided by oidc when it has a step-up`() {
        val oidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 4,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns oidcTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "4"
    }
}

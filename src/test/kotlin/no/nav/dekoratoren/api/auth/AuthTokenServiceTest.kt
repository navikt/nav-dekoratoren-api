package no.nav.dekoratoren.api.auth

import io.ktor.server.application.ApplicationCall
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.oidc.OidcTokenService
import no.nav.dekoratoren.api.selfissued.SelfIssuedTokenService
import no.nav.dekoratoren.api.user.SubjectNameService
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class AuthTokenServiceTest {

    private val subject1 = "123"
    private val subject2 = "456"

    private val subject1Name = "oneTwoThree"
    private val subject2Name = "fourFiveSix"

    private val oidcTokenService: OidcTokenService = mockk()
    private val subjectNameService: SubjectNameService = mockk()
    private val selfIssuedTokenService: SelfIssuedTokenService = mockk()

    private val authTokenService = no.nav.dekoratoren.api.auth.AuthTokenService(
        oidcTokenService,
        subjectNameService,
        selfIssuedTokenService,
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
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "3"
    }

    @Test
    fun `should provide correct info for self issued token`() {
        val tokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 4,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns tokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "4"
    }

    @Test
    fun `should provide correct info when unauthenticated`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns null
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
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "4"
    }

    @Test
    fun `should defer to oidc token if it is newer than self issued token`() {
        val oidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )
        val selfIssuedTokenInfo = OidcTokenInfo(
            subject = subject2,
            authLevel = 4,
            issueTime = LocalDateTime.now().minusMinutes(5),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns oidcTokenInfo
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns selfIssuedTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { subjectNameService.getSubjectName(subject2) } returns subject2Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "3"
    }

    @Test
    fun `should defer to self-issued token if it is newer than oidc token`() {
        val oidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now().minusMinutes(5),
            expiryTime = LocalDateTime.now().plusDays(1)
        )
        val selfIssuedTokenInfo = OidcTokenInfo(
            subject = subject2,
            authLevel = 4,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns oidcTokenInfo
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } returns selfIssuedTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { subjectNameService.getSubjectName(subject2) } returns subject2Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject2Name
        subjectInfo.securityLevel `should be equal to` "4"
    }

    @Test
    fun `should claim user is unauthenticated if self-issued token service throws error and oidc service is ok`() {
        val subject1OidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns subject1OidcTokenInfo
        coEvery { selfIssuedTokenService.getSelfIssuedToken(call) } throws Exception()
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` false
        subjectInfo.name `should be equal to` null
        subjectInfo.securityLevel `should be equal to` null
    }
}

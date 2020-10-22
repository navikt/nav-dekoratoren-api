package no.nav.personbruker.innloggingsstatus.auth

import io.ktor.application.ApplicationCall
import io.ktor.util.KtorExperimentalAPI
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.innloggingsstatus.common.metrics.MetricsCollector
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.personbruker.innloggingsstatus.oidc.OidcTokenService
import no.nav.personbruker.innloggingsstatus.openam.OpenAMTokenInfo
import no.nav.personbruker.innloggingsstatus.openam.OpenAMTokenService
import no.nav.personbruker.innloggingsstatus.user.SubjectNameService
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

@KtorExperimentalAPI
internal class AuthTokenServiceTest {

    private val subject1 = "123"
    private val subject2 = "456"

    private val subject1Name = "oneTwoThree"
    private val subject2Name = "fourFiveSix"

    private val oidcTokenService: OidcTokenService = mockk()
    private val openAMTokenService: OpenAMTokenService = mockk()
    private val subjectNameService: SubjectNameService = mockk()
    private val metricsCollector: MetricsCollector = mockk()

    private val authTokenService = AuthTokenService(oidcTokenService, openAMTokenService, subjectNameService, metricsCollector)

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
        coEvery { openAMTokenService.getOpenAMToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "3"
    }

    @Test
    fun `should provide correct info for openAm token`() {
        val tokenInfo = OpenAMTokenInfo(
            subject = subject1,
            authLevel = 3
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { openAMTokenService.getOpenAMToken(call) } returns tokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "3"
    }

    @Test
    fun `should provide correct info when unathenticated`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { openAMTokenService.getOpenAMToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(any()) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

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

        val openAmTokenInfo = OpenAMTokenInfo(
            subject = subject1,
            authLevel = 3
        )


        coEvery { oidcTokenService.getOidcToken(call) } returns oidcTokenInfo
        coEvery { openAMTokenService.getOpenAMToken(call) } returns openAmTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "4"
    }

    @Test
    fun `should defer to security level provided by openAM when it has a step-up`() {
        val oidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        val openAmTokenInfo = OpenAMTokenInfo(
            subject = subject1,
            authLevel = 4
        )


        coEvery { oidcTokenService.getOidcToken(call) } returns oidcTokenInfo
        coEvery { openAMTokenService.getOpenAMToken(call) } returns openAmTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` true
        subjectInfo.name `should be equal to` subject1Name
        subjectInfo.securityLevel `should be equal to` "4"
    }

    @Test
    fun `should consider user to be unauthenticated if we find valid authentication for two different users`() {
        val subject1OidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        val subject2OpenAmTokenInfo = OpenAMTokenInfo(
            subject = subject2,
            authLevel = 4
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns subject1OidcTokenInfo
        coEvery { openAMTokenService.getOpenAMToken(call) } returns subject2OpenAmTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { subjectNameService.getSubjectName(subject2) } returns subject2Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` false
        subjectInfo.name `should be equal to` null
        subjectInfo.securityLevel `should be equal to` null
    }

    @Test
    fun `should claim user is unauthenticated if oidc service throws error and openAM service is OK`() {
        val subject2OpenAmTokenInfo = OpenAMTokenInfo(
            subject = subject2,
            authLevel = 4
        )

        coEvery { oidcTokenService.getOidcToken(call) } throws Exception()
        coEvery { openAMTokenService.getOpenAMToken(call) } returns subject2OpenAmTokenInfo
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` false
        subjectInfo.name `should be equal to` null
        subjectInfo.securityLevel `should be equal to` null
    }

    @Test
    fun `should claim user is unauthenticated if openAM service throws error and oidc service is OK`() {
        val subject1OidcTokenInfo = OidcTokenInfo(
            subject = subject1,
            authLevel = 3,
            issueTime = LocalDateTime.now(),
            expiryTime = LocalDateTime.now().plusDays(1)
        )

        coEvery { oidcTokenService.getOidcToken(call) } returns subject1OidcTokenInfo
        coEvery { openAMTokenService.getOpenAMToken(call) } throws Exception()
        coEvery { subjectNameService.getSubjectName(subject1) } returns subject1Name
        coEvery { metricsCollector.recordAuthMetrics(any(), any()) } returns Unit

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        subjectInfo.authenticated `should be equal to` false
        subjectInfo.name `should be equal to` null
        subjectInfo.securityLevel `should be equal to` null
    }

}
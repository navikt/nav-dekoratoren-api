package no.nav.dekoratoren.api.innloggingsstatus.auth

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.ktor.server.application.ApplicationCall
import io.mockk.coEvery
import io.mockk.mockk
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenInfo
import no.nav.dekoratoren.api.innloggingsstatus.oidc.OidcTokenService
import no.nav.dekoratoren.api.innloggingsstatus.user.SubjectNameService
import org.junit.jupiter.api.Test

class AuthTokenServiceTest {

    private val oidcTokenService: OidcTokenService = mockk()
    private val subjectNameService: SubjectNameService = mockk()

    private val authTokenService = AuthTokenService(
        oidcTokenService,
        subjectNameService,
    )

    private val call: ApplicationCall = mockk()

    @Test
    fun `should provide correct info for oidc token`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns dummyOidcTokenInfo(3)
        coEvery { subjectNameService.getSubjectName(SUBJECT) } returns SUBJECT_NAME

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        assertSoftly(subjectInfo) {
            authenticated.shouldBeTrue()
            name shouldBe SUBJECT_NAME
            securityLevel shouldBe "3"
        }
    }

    @Test
    fun `should provide correct info when unauthenticated`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns null
        coEvery { subjectNameService.getSubjectName(any()) } returns SUBJECT_NAME

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        assertSoftly(subjectInfo) {
            authenticated.shouldBeFalse()
            name.shouldBeNull()
            securityLevel.shouldBeNull()
        }
    }

    @Test
    fun `should defer to security level provided by oidc when it has a step-up`() {
        coEvery { oidcTokenService.getOidcToken(call) } returns dummyOidcTokenInfo(4)
        coEvery { subjectNameService.getSubjectName(SUBJECT) } returns SUBJECT_NAME

        val subjectInfo = runBlocking { authTokenService.getAuthenticatedUserInfo(call) }

        assertSoftly(subjectInfo) {
            authenticated.shouldBeTrue()
            name shouldBe SUBJECT_NAME
            securityLevel shouldBe "4"
        }
    }

    private fun dummyOidcTokenInfo(authLevel: Int) = OidcTokenInfo(
        subject = SUBJECT,
        authLevel = authLevel,
        issueTime = LocalDateTime.now(),
        expiryTime = LocalDateTime.now().plusDays(1)
    )

    companion object {
        private val SUBJECT = "123"
        private val SUBJECT_NAME = "oneTwoThree"
    }
}

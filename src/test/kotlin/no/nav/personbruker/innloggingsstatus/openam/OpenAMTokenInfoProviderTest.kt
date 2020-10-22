package no.nav.personbruker.innloggingsstatus.openam

import io.mockk.*
import kotlinx.coroutines.runBlocking
import no.nav.personbruker.innloggingsstatus.cache.MockedEvictingCacheFactory
import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

internal class OpenAMTokenInfoProviderTest {

    private val subject = "123456"
    private val authLevel = 4
    private val essoToken = "token"

    private val openAMConsumer: OpenAMConsumer = mockk()

    private val shallowCache = MockedEvictingCacheFactory.createShallowCache<String, OpenAMTokenInfo>()
    private val cachingProvider = CachingOpenAmTokenInfoProvider(openAMConsumer, shallowCache)

    private val nonCachingProvider = NonCachingOpenAmTokenInfoProvider(openAMConsumer)

    @AfterEach
    fun cleanUp() {
        clearMocks(openAMConsumer)
    }

    @Test
    fun `Non caching provider should fetch and map token info correctly`() {
        coEvery { openAMConsumer.getOpenAMTokenAttributes(essoToken) } returns
                OpenAMTokenResponseObjectMother.createOpenAMTokenResponse(essoToken, subject, authLevel)

        val result = runBlocking { nonCachingProvider.getTokenInfo(essoToken) }

        result?.subject `should be equal to` subject
        result?.authLevel `should be equal to` authLevel
    }

    @Test
    fun `Caching provider should fetch and map token info correctly, but should defer to its cache first`() {
        coEvery { openAMConsumer.getOpenAMTokenAttributes(essoToken) } returns
                OpenAMTokenResponseObjectMother.createOpenAMTokenResponse(essoToken, subject, authLevel)

        val result = runBlocking { cachingProvider.getTokenInfo(essoToken) }

        result?.subject `should be equal to` subject
        result?.authLevel `should be equal to` authLevel

        coVerifyOrder {
            shallowCache.getEntry(essoToken, any())
            openAMConsumer.getOpenAMTokenAttributes(essoToken)
        }
    }

    @Test
    fun `Non caching provider should return null on error response`() {
        coEvery { openAMConsumer.getOpenAMTokenAttributes(essoToken) } returns OpenAMResponse.errorResponse()

        val result = runBlocking { nonCachingProvider.getTokenInfo(essoToken) }

        result `should be equal to` null
    }

    @Test
    fun `Caching provider should return null on error response, but should defer to its cache first`() {
        coEvery { openAMConsumer.getOpenAMTokenAttributes(essoToken) } returns OpenAMResponse.errorResponse()

        val result = runBlocking { cachingProvider.getTokenInfo(essoToken) }

        result `should be equal to` null

        coVerifyOrder {
            shallowCache.getEntry(essoToken, any())
            openAMConsumer.getOpenAMTokenAttributes(essoToken)
        }
    }
}
package no.nav.dekoratoren.api.common

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class UrlPartUtilTest {

    @Test
    fun `Should parse domain from origin url, leaving out scheme`() {
        val domain = "sub.test.com"

        val httpUrl = "http://$domain"
        val httpsUrl = "https://$domain"

        UrlPartUtil.parseDomain(httpUrl) `should be equal to` domain
        UrlPartUtil.parseDomain(httpsUrl) `should be equal to` domain
    }

    @Test
    fun `Should parse domain from referrer url, leaving out scheme, path and params`() {
        val domain = "sub.test.com"

        val url = "https://$domain/path/to/something?param=123"

        UrlPartUtil.parseDomain(url) `should be equal to` domain
    }
}
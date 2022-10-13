package no.nav.dekoratoren.api.common

import org.amshove.kluent.`should be equal to`
import org.junit.jupiter.api.Test

internal class Base64KtTest {

    @Test
    fun encodingAndDecodingSameStringShouldBeANoop() {
        val original = "test123"
        val result = original.toBase64().fromBase64()

        original `should be equal to` result
    }
}
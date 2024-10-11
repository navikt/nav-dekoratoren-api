package no.nav.dekoratoren.api.common

import java.util.Base64

fun String.toBase64() = encodeBase64(this)

fun encodeBase64(string: String): String {
    return string.toByteArray(charset("UTF-8")).let { bytes ->
        Base64.getEncoder().encodeToString(bytes)
    }
}

fun String.fromBase64() = decodeBase64(this)

fun decodeBase64(string: String): String {
    return string.toByteArray(charset("UTF-8")).let { bytes ->
        Base64.getDecoder().decode(bytes)
    }.let { encodedBytes ->
        String(encodedBytes)
    }
}
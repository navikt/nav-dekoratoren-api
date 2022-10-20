package no.nav.dekoratoren.api.varsel

import io.ktor.http.*

class RequestContent(
    val rawContent: ByteArray,
    val contentType: ContentType
)

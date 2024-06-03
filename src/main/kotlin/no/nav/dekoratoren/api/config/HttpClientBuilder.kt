package no.nav.dekoratoren.api.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.jackson.jackson

object HttpClientBuilder {

    fun build(): HttpClient {
        return HttpClient(Apache) {
            install(ContentNegotiation) {
                jackson {
                    registerModule(JavaTimeModule())
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                }
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 1000
            }
        }
    }
}
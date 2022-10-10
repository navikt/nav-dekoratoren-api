package no.nav.dekoratoren.api.health

interface SelfTest {
    suspend fun externalServiceStatus(): ServiceStatus
    val externalServiceName: String
}

enum class ServiceStatus {
    OK, ERROR
}
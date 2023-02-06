object Caffeine {
    private const val version = "3.1.2"
    private const val groupId = "com.github.ben-manes.caffeine"

    const val caffeine = "$groupId:caffeine:$version"
}

object DittNAV {
    object Common {
        private const val version = "2022.09.30-12.41-aa46d2d75788"
        private const val groupId = "com.github.navikt.dittnav-common-lib"

        const val utils = "$groupId:dittnav-common-utils:$version"
    }
}

object Finn {
    private const val version = "4.4.1"
    private const val groupId = "no.finn.unleash"

    const val unleashClient = "$groupId:unleash-client-java:$version"
}

object Jackson {
    private const val version = "2.14.2"

    const val dataTypeJsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$version"
    const val moduleKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
}

object Junit {
    private const val version = "5.9.2"
    private const val groupId = "org.junit.jupiter"

    const val api = "$groupId:junit-jupiter-api:$version"
    const val engine = "$groupId:junit-jupiter-engine:$version"
}

object Kluent {
    private const val version = "1.72"
    const val kluent = "org.amshove.kluent:kluent:$version"
}

object Kotlin {
    const val version = "1.8.10"
}

object Kotlinx {
    private const val groupId = "org.jetbrains.kotlinx"

    const val coroutines = "$groupId:kotlinx-coroutines-core:1.6.4"
    const val htmlJvm = "$groupId:kotlinx-html-jvm:0.8.0"
}

object Ktor {
    private const val version = "2.2.3"
    private const val groupId = "io.ktor"

    const val metricsMicrometer = "$groupId:ktor-server-metrics-micrometer:$version"
    const val serverNetty = "$groupId:ktor-server-netty:$version"
    const val clientApache = "$groupId:ktor-client-apache:$version"
    const val clientJson = "$groupId:ktor-client-json:$version"
    const val clientSerializationJvm = "$groupId:ktor-client-serialization-jvm:$version"
    const val clientJackson = "$groupId:ktor-client-jackson:$version"
    const val clientLogging = "$groupId:ktor-client-logging:$version"
    const val clientLoggingJvm = "$groupId:ktor-client-logging-jvm:$version"
    const val clientMock = "$groupId:ktor-client-mock:$version"
    const val clientMockJvm = "$groupId:ktor-client-mock-jvm:$version"
    const val clientContentNegotiation = "$groupId:ktor-client-content-negotiation:$version"
    const val serverContentNegotiation = "$groupId:ktor-server-content-negotiation:$version"
    const val htmlBuilder = "$groupId:ktor-server-html-builder:$version"
    const val jackson = "$groupId:ktor-serialization-jackson:$version"
    const val defaultHeaders = "$groupId:ktor-server-default-headers:$version"
    const val cors = "$groupId:ktor-server-cors:$version"
    const val testHostJvm = "$groupId:ktor-server-test-host-jvm:$version"
}

object Logback {
    private const val version = "1.4.5"
    const val classic = "ch.qos.logback:logback-classic:$version"
}

object Logstash {
    private const val version = "7.2"
    const val logbackEncoder = "net.logstash.logback:logstash-logback-encoder:$version"
}

object Micrometer {
    private const val version = "1.10.3"
    const val registryPrometheus = "io.micrometer:micrometer-registry-prometheus:$version"
}

object Mockk {
    private const val version = "1.13.4"
    const val mockk = "io.mockk:mockk:$version"
}

object NAV {
    object Security {
        private const val version = "3.0.2"
        private const val groupId = "no.nav.security"

        const val tokenValidationKtor = "$groupId:token-validation-ktor-v2:$version"
    }

    object Common {
        private const val version = "2.2023.01.10_13.49-81ddc732df3a"
        private const val groupId = "no.nav.common"
        const val featureToggle = "$groupId:feature-toggle:$version"
    }
}

object Shadow {
    const val version = "7.1.2"
    const val pluginId = "com.github.johnrengelman.shadow"
}

object TmsKtorTokenSupport {
    private const val version = "2.1.0"
    private const val groupId = "com.github.navikt.tms-ktor-token-support"

    const val azureExchange = "$groupId:token-support-azure-exchange:$version"
}

object Versions {
    const val version = "0.45.0"
    const val pluginId = "com.github.ben-manes.versions"
}

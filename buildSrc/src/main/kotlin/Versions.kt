object Caffeine {

    private const val version = "3.1.0"
    private const val groupId = "com.github.ben-manes.caffeine"

    const val caffeine = "$groupId:caffeine:$version"
}

object DittNAV {
    object Common {
        private const val version = "2022.04.19-11.11-1043a85c4f6f"
        private const val groupId = "com.github.navikt.dittnav-common-lib"

        const val logging = "$groupId:dittnav-common-logging:$version"
        const val influx = "$groupId:dittnav-common-influx:$version"
        const val utils = "$groupId:dittnav-common-utils:$version"
    }
}

object Jackson {
    private const val version = "2.13.2"

    const val dataTypeJsr310 = "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$version"
    const val moduleKotlin = "com.fasterxml.jackson.module:jackson-module-kotlin:$version"
}

object Junit {
    private const val version = "5.8.2"
    private const val groupId = "org.junit.jupiter"

    const val api = "$groupId:junit-jupiter-api:$version"
    const val engine = "$groupId:junit-jupiter-engine:$version"
}

object Kluent {
    private const val version = "1.68"
    const val kluent = "org.amshove.kluent:kluent:$version"
}

object Kotlin {
    const val version = "1.6.21"
}

object Kotlinx {
    private const val groupId = "org.jetbrains.kotlinx"

    const val coroutines = "$groupId:kotlinx-coroutines-core:1.6.1"
    const val htmlJvm = "$groupId:kotlinx-html-jvm:0.7.5"
}

object Ktor {
    private const val version = "2.0.1"
    private const val groupId = "io.ktor"

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
}

object Logback {
    private const val version = "1.2.11"
    const val classic = "ch.qos.logback:logback-classic:$version"
}

object Logstash {
    private const val version = "7.1.1"
    const val logbackEncoder = "net.logstash.logback:logstash-logback-encoder:$version"
}

object Mockk {
    private const val version = "1.12.3"
    const val mockk = "io.mockk:mockk:$version"
}

object NAV {
    const val tokenValidatorKtor = "no.nav.security:token-validation-ktor:2.0.15"
}

object Prometheus {
    private const val version = "0.15.0"
    private const val groupId = "io.prometheus"

    const val common = "$groupId:simpleclient_common:$version"
    const val hotspot = "$groupId:simpleclient_hotspot:$version"
    const val logback = "$groupId:simpleclient_logback:$version"
}

object Shadow {
    const val version = "7.1.2"
    const val pluginId = "com.github.johnrengelman.shadow"
}

object Versions {
    const val version = "0.42.0"
    const val pluginId = "com.github.ben-manes.versions"
}
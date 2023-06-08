import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    kotlin("jvm") version("1.8.21")
    kotlin("plugin.allopen") version("1.8.21")

    id("com.github.johnrengelman.shadow") version("8.1.1")
    id("com.github.ben-manes.versions") version("0.46.0") // ./gradlew dependencyUpdates to check for new versions
    application
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    val versions = object {
        val caffeine = "3.1.6"
        val dittnavCommon = "2022.09.30-12.41-aa46d2d75788"
        val tmsKtorTokenSupport = "2.0.1"
        val unleash = "4.4.1"
        val jackson = "2.15.2"
        val junit = "5.9.3"
        val kluent = "1.73"
        val kotlin = "1.8.21"
        val kotlinxCoroutines = "1.7.1"
        val kotlinxHtmlJvm = "0.8.1"
        val ktor = "2.3.1"
        val logback = "1.4.7"
        val logstash = "7.3"
        val micrometer = "1.11.0"
        val mockk = "1.13.5"
        val navSecurity = "3.1.0"
        val navCommon = "2.2023.01.10_13.49-81ddc732df3a"
    }

    implementation("com.github.navikt.dittnav-common-lib:dittnav-common-utils:${versions.dittnavCommon}")
    implementation("com.github.navikt.tms-ktor-token-support:token-support-azure-exchange:${versions.tmsKtorTokenSupport}")
    implementation("com.github.ben-manes.caffeine:caffeine:${versions.caffeine}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${versions.jackson}")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${versions.jackson}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${versions.kotlinxCoroutines}")
    implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:${versions.kotlinxHtmlJvm}")
    implementation("io.ktor:ktor-client-apache:${versions.ktor}")
    implementation("io.ktor:ktor-client-jackson:${versions.ktor}")
    implementation("io.ktor:ktor-client-json:${versions.ktor}")
    implementation("io.ktor:ktor-client-logging:${versions.ktor}")
    implementation("io.ktor:ktor-client-logging-jvm:${versions.ktor}")
    implementation("io.ktor:ktor-client-serialization-jvm:${versions.ktor}")
    implementation("io.ktor:ktor-client-content-negotiation:${versions.ktor}")
    implementation("io.ktor:ktor-server-content-negotiation:${versions.ktor}")
    implementation("io.ktor:ktor-server-html-builder:${versions.ktor}")
    implementation("io.ktor:ktor-serialization-jackson:${versions.ktor}")
    implementation("io.ktor:ktor-server-netty:${versions.ktor}")
    implementation("io.ktor:ktor-server-default-headers:${versions.ktor}")
    implementation("io.ktor:ktor-server-cors:${versions.ktor}")
    implementation("io.ktor:ktor-server-metrics-micrometer:${versions.ktor}")
    implementation("io.micrometer:micrometer-registry-prometheus:${versions.micrometer}")
    implementation("ch.qos.logback:logback-classic:${versions.logback}")
    implementation("net.logstash.logback:logstash-logback-encoder:${versions.logstash}")
    implementation("no.nav.security:token-validation-ktor-v2:${versions.navSecurity}")
    implementation("no.nav.common:feature-toggle:${versions.navCommon}")
    implementation("no.finn.unleash:unleash-client-java:${versions.unleash}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${versions.junit}")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:${versions.junit}")
    testImplementation("org.amshove.kluent:kluent:${versions.kluent}")
    testImplementation("io.ktor:ktor-client-mock:${versions.ktor}")
    testImplementation("io.ktor:ktor-client-mock-jvm:${versions.ktor}")
    testImplementation("io.ktor:ktor-server-test-host-jvm:${versions.ktor}")
    testImplementation("io.mockk:mockk:${versions.mockk}")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks {
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events("passed", "skipped", "failed")
        }
    }
}

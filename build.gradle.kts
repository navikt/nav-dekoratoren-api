plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.allopen") version Kotlin.version

    id(Shadow.pluginId) version Shadow.version
    id(Versions.pluginId) version Versions.version // ./gradlew dependencyUpdates to check for new versions
    application
}

kotlin {
    jvmToolchain(17)
}

repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation(DittNAV.Common.utils)
    implementation(TmsKtorTokenSupport.azureExchange)
    implementation(Caffeine.caffeine)
    implementation(Jackson.dataTypeJsr310)
    implementation(Jackson.moduleKotlin)
    implementation(Kotlinx.coroutines)
    implementation(Kotlinx.htmlJvm)
    implementation(Ktor.clientApache)
    implementation(Ktor.clientJackson)
    implementation(Ktor.clientJson)
    implementation(Ktor.clientLogging)
    implementation(Ktor.clientLoggingJvm)
    implementation(Ktor.clientSerializationJvm)
    implementation(Ktor.clientContentNegotiation)
    implementation(Ktor.serverContentNegotiation)
    implementation(Ktor.htmlBuilder)
    implementation(Ktor.jackson)
    implementation(Ktor.serverNetty)
    implementation(Ktor.defaultHeaders)
    implementation(Ktor.cors)
    implementation(Ktor.metricsMicrometer)
    implementation(Micrometer.registryPrometheus)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)
    implementation(NAV.Security.tokenValidationKtor)
    implementation(NAV.Common.featureToggle)
    implementation(Finn.unleashClient)
    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(Kluent.kluent)
    testImplementation(Ktor.clientMock)
    testImplementation(Ktor.clientMockJvm)
    testImplementation(Ktor.testHostJvm)
    testImplementation(Mockk.mockk)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks {

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

apply(plugin = Shadow.pluginId)

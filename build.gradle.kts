plugins {
    kotlin("jvm") version Kotlin.version
    kotlin("plugin.allopen") version Kotlin.version

    id(Shadow.pluginId) version Shadow.version
    id(Versions.pluginId) version Versions.version // ./gradlew dependencyUpdates to check for new versions
    application
}

repositories {
    maven { url = uri("https://jitpack.io") }
    mavenCentral()
}

dependencies {
    implementation(DittNAV.Common.logging)
    implementation(DittNAV.Common.utils)
    implementation(DittNAV.Common.influx)
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
    implementation(Ktor.htmlBuilder)
    implementation(Ktor.jackson)
    implementation(Ktor.serverNetty)
    implementation(Logback.classic)
    implementation(Logstash.logbackEncoder)
    implementation(NAV.tokenValidatorKtor)
    implementation(NAV.customKtorCorsFeature)
    implementation(Prometheus.common)
    implementation(Prometheus.hotspot)
    implementation(Prometheus.logback)
    testImplementation(Junit.api)
    testImplementation(Junit.engine)
    testImplementation(Kluent.kluent)
    testImplementation(Ktor.clientMock)
    testImplementation(Ktor.clientMockJvm)
    testImplementation(Mockk.mockk)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "17"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
}

tasks {

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    register("runServer", JavaExec::class) {
        environment("OIDC_ISSUER", "http://localhost:9000")
        environment("LOGINSERVICE_IDPORTEN_DISCOVERY_URL", "http://localhost:9000/.well-known/openid-configuration")
        environment("LOGINSERVICE_IDPORTEN_AUDIENCE", "stubOidcClient")
        environment("OPEN_AM_REST_SERVICE_URL", "http://localhost:8095/esso")
        environment("SECURITY_TOKEN_SERVICE_URL", "http://localhost:8095/security-token-service-token")
        environment("STS_API_GW_KEY", "stsKey")
        environment("PDL_API_URL", "http://localhost:8095/pdl-api")
        environment("PDL_API_GW_KEY", "pdlKey")
        environment("SERVICEUSER_USERNAME", "username")
        environment("SERVICEUSER_PASSWORD", "password")
        environment("NAIS_CLUSTER_NAME", "dev-sbs")
        environment("NAIS_NAMESPACE", "local")
        environment("SENSU_HOST", "stub")
        environment("SENSU_PORT", "0")
        environment("STS_CACHE_ENABLED", "false")
        environment("STS_CACHE_EXPIRY_MARGIN_MINUTES", "5")
        environment("CORS_ALLOWED_HOST", "*")
        environment("OIDC_CLAIM_CONTAINING_THE_IDENTITY", "pid")

        main = application.mainClassName
        classpath = sourceSets["main"].runtimeClasspath
    }
}

apply(plugin = Shadow.pluginId)

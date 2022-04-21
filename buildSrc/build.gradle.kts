plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val dittNavDependenciesVersion = "2022.03.07-09.11-113dabef3447"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}

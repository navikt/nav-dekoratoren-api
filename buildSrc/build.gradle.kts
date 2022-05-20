plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val dittNavDependenciesVersion = "2022.04.19-11.23-4cfb71596169"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}

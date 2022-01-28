plugins {
    `kotlin-dsl`
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

val dittNavDependenciesVersion = "2022.01.25-11.54-e80d38e26f13"

dependencies {
    implementation("com.github.navikt:dittnav-dependencies:$dittNavDependenciesVersion")
}

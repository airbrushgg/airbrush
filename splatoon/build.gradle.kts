plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.1"
}

group = "gg.airbrush"
version = "0.1.0"

val minestomVersion: String by rootProject.extra

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly(project(":server"))
    compileOnly(project(":worlds"))
}

kotlin {
    jvmToolchain(21)
}
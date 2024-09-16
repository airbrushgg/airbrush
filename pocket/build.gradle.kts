plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "gg.airbrush"
version = "1.0.0"

val workaroundVersion = version as String
val minestomVersion: String by rootProject.extra

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
}
plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "gg.airbrush"
version = "1.0.0"
val workaroundVersion = version as String

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly("dev.hollowcube:minestom-ce:010fe985bb")
}
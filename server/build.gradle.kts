plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    `maven-publish`
    id("com.palantir.git-version") version "3.0.0"
    application
}

group = "gg.airbrush"
version = "0.3.2"

val workaroundVersion = version as String

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:1f34e60ea6")
    implementation("dev.hollowcube:polar:1.11.0")
    implementation(project(":bamboo"))
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("cc.ekblad:4koma:1.2.0")
    implementation("org.apache.logging.log4j:log4j-api:2.17.2")
    implementation("org.apache.logging.log4j:log4j-core:2.17.2")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.14.1")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.2.0")
    implementation("org.slf4j:slf4j-api:2.0.9")
    implementation("org.slf4j:slf4j-log4j12:2.0.9")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("gg.airbrush.server.MainKt")
}

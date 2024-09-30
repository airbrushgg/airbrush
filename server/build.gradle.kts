import org.gradle.api.tasks.Copy

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.1"
    `maven-publish`
    id("com.palantir.git-version") version "3.0.0"
    application
}

group = "gg.airbrush"
version = "0.3.2"

val workaroundVersion = version as String
val minestomVersion: String by rootProject.extra

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("net.minestom:minestom-snapshots:$minestomVersion")
    implementation("dev.hollowcube:polar:1.11.2")
    implementation("dev.flavored:bamboo:1.1.0")
    implementation("net.kyori:adventure-text-minimessage:4.14.0")
    implementation("cc.ekblad:4koma:1.2.0")
    implementation("ch.qos.logback:logback-core:1.5.8")
    implementation("ch.qos.logback:logback-classic:1.5.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}

application {
    mainClass.set("gg.airbrush.server.MainKt")
}

tasks.register<Copy>("moveJar") {
    val sourceDir = file("${layout.buildDirectory.asFile.get().path}/libs")
    val destinationDir = file("../dev-env/")

    destinationDir.mkdirs()

    from(sourceDir) {
        include { details ->
            details.file.name == "airbrush.jar"
        }
    }

    into(destinationDir)
}

tasks.shadowJar {
    archiveFileName.set("airbrush.jar")
    finalizedBy(tasks.named("moveJar"))
}
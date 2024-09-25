plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("com.palantir.git-version") version "3.0.0"
    id("com.gradleup.shadow") version "8.3.1"
}

group = "gg.airbrush"
version = "1.0-SNAPSHOT"

val minestomVersion: String by rootProject.extra
val jarName = "discord.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
	implementation("net.dv8tion:JDA:5.1.0") {
		exclude("opus-java")
	}

    compileOnly(project(":server"))
    compileOnly(project(":sdk"))

    compileOnly("cc.ekblad:4koma:1.2.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")

    compileOnly("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")

	implementation("me.santio.Coffee:jda:85d9b1e6d5")
}

tasks.register<Copy>("moveJar") {
    val sourceDir = file("${layout.buildDirectory.asFile.get().path}/libs")
    val destinationDir = file("../dev-env/plugins")

    destinationDir.mkdirs()

    from(sourceDir) {
        include { details ->
            details.file.name == jarName
        }
    }

    into(destinationDir)
}

tasks.shadowJar {
    archiveFileName.set(jarName)
    finalizedBy(tasks.named("moveJar"))
}
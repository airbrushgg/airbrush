plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "gg.airbrush"
version = "1.0.0"

val workaroundVersion = version as String
val minestomVersion: String by rootProject.extra
val jarName = "pocket.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
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

tasks.jar {
    archiveFileName.set(jarName)
    finalizedBy(tasks.named("moveJar"))
}
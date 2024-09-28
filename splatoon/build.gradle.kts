plugins {
    kotlin("jvm")
    id("com.gradleup.shadow") version "8.3.1"
}

val minestomVersion: String by rootProject.extra
val jarName = "splatoon.jar"

group = "gg.airbrush"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly(project(":server"))
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
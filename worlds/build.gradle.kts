plugins {
    kotlin("jvm") version "2.0.10"
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.1"
    id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "0.1.0"

val minestomVersion: String by rootProject.extra
val jarName = "worlds.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly(project(":sdk"))
    compileOnly("dev.flavored:bamboo:1.1.0")
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly("dev.hollowcube:polar:1.11.2")
    compileOnly("cc.ekblad:4koma:1.2.0")
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
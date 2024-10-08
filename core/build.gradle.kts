plugins {
	kotlin("jvm") version "2.0.0"
	id("com.gradleup.shadow") version "8.3.1"
	kotlin("plugin.serialization") version "1.9.0"
	id("com.palantir.git-version") version "3.0.0"
	`maven-publish`
}

group = "gg.airbrush"
version = "0.1.0"

val minestomVersion: String by rootProject.extra
val jarName = "core.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":sdk"))
    compileOnly(project(":pocket"))
    compileOnly(project(":server"))
    compileOnly(project(":worlds"))
    compileOnly(project(":punishments"))
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")
    compileOnly("dev.flavored:bamboo:1.1.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    compileOnly("dev.hollowcube:polar:1.11.2")
    compileOnly("cc.ekblad:4koma:1.2.0")
    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

	compileOnly(project(":discord"))
	compileOnly("net.dv8tion:JDA:5.1.0")
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

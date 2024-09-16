import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.1"
	id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "0.2.0"

val minestomVersion: String by rootProject.extra
val jarName = "permissions.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly(project(":server"))
    compileOnly(project(":sdk"))
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
}

tasks.withType(ShadowJar::class.java) {
    dependencies {
        exclude {
            it.moduleGroup == "org.jetbrains.kotlin"
        }
    }
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
plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.1"
    id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "1.0-SNAPSHOT"

val minestomVersion: String by rootProject.extra
val jarName = "punishments.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	compileOnly(project(":server"))
	compileOnly(project(":sdk"))
    compileOnly(project(":pocket"))
	compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
	compileOnly("cc.ekblad:4koma:1.2.0")
	compileOnly(project(":core"))
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
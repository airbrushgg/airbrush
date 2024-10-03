plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.1"
    `maven-publish`
    kotlin("plugin.serialization") version "2.0.0"
    id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "0.2.0"

val workaroundVersion = version as String
val minestomVersion: String by rootProject.extra
val jarName = "sdk.jar"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly("cc.ekblad:4koma:1.2.0")

    // db
    implementation("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")
    implementation("org.xerial:sqlite-jdbc:3.46.1.3")
    implementation("gg.ingot:iron:2859983d8f")

    compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // caching
    implementation("com.github.ben-manes.caffeine:caffeine:3.0.4")

    // Used for translations, needed for its lack of type-safety :kek:
	implementation("com.moandjiezana.toml:toml4j:0.7.2")
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
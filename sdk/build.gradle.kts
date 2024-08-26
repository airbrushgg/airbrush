plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "7.0.0"
    application
    `maven-publish`
    kotlin("plugin.serialization") version "1.9.0"
    id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "0.2.0"
val workaroundVersion = version as String

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server"))
    compileOnly("net.minestom:minestom-snapshots:1f34e60ea6")
    compileOnly("cc.ekblad:4koma:1.2.0")
    implementation("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
	// Used for translations, needed for its lack of type-safety :kek:
	implementation("com.moandjiezana.toml:toml4j:0.7.2")
}

kotlin {
    jvmToolchain(21)
}

application {
    mainClass.set("MainKt")
}

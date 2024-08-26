plugins {
	kotlin("jvm") version "2.0.0"
	id("com.github.johnrengelman.shadow") version "7.0.0"
	kotlin("plugin.serialization") version "1.9.0"
	id("com.palantir.git-version") version "3.0.0"
	`maven-publish`
}

group = "gg.airbrush"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":sdk"))
    compileOnly(project(":pocket"))
    compileOnly(project(":server"))
    compileOnly(project(":worlds"))
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.minestom:minestom-snapshots:1f34e60ea6")
    compileOnly("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")
    compileOnly("dev.flavored:bamboo:0.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    compileOnly("dev.hollowcube:polar:1.3.2")
    compileOnly("cc.ekblad:4koma:1.2.0")

	compileOnly(project(":discord"))
	compileOnly("net.dv8tion:JDA:5.0.0-beta.15")
}

kotlin {
    jvmToolchain(21)
}

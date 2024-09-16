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
    compileOnly("net.minestom:minestom-snapshots:$minestomVersion")
    compileOnly("org.mongodb:mongodb-driver-kotlin-sync:4.10.1")
    compileOnly("dev.flavored:bamboo:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
    compileOnly("dev.hollowcube:polar:1.11.2")
    compileOnly("cc.ekblad:4koma:1.2.0")

	compileOnly(project(":discord"))
	compileOnly("net.dv8tion:JDA:5.1.0")
}
plugins {
    kotlin("jvm") version "2.0.0"
    application
    `maven-publish`
    id("com.palantir.git-version") version "3.0.0"
    id("com.gradleup.shadow") version "8.3.1"
}

group = "gg.airbrush"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

	implementation("net.dv8tion:JDA:5.0.0-beta.15") {
		exclude("opus-java")
	}

    compileOnly(project(":server"))
    compileOnly(project(":sdk"))

    compileOnly("cc.ekblad:4koma:1.2.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.minestom:minestom-snapshots:1f34e60ea6")

	implementation("me.santio.Coffee:jda:85d9b1e6d5")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}

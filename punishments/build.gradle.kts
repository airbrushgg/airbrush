plugins {
    kotlin("jvm") version "2.0.0"
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	compileOnly(project(":server"))
	compileOnly(project(":sdk"))
	compileOnly("dev.hollowcube:minestom-ce:010fe985bb")
	compileOnly("cc.ekblad:4koma:1.2.0")
	compileOnly(project(":core"))
	compileOnly(project(":discord"))
	compileOnly("net.dv8tion:JDA:5.0.0-beta.15")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(19)
}

application {
    mainClass.set("MainKt")
}
plugins {
    kotlin("jvm") version "2.0.0"
    application
    `maven-publish`
    id("com.gradleup.shadow") version "8.3.1"
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
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
	compileOnly(project(":server"))
	compileOnly(project(":sdk"))
	compileOnly("net.minestom:minestom-snapshots:1f34e60ea6")
	compileOnly("cc.ekblad:4koma:1.2.0")
	compileOnly(project(":core"))
	compileOnly(project(":discord"))
	compileOnly("net.dv8tion:JDA:5.1.0")
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainClass.set("MainKt")
}
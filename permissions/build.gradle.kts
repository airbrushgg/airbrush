import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    kotlin("jvm") version "2.0.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
	id("com.palantir.git-version") version "3.0.0"
}

group = "gg.airbrush"
version = "0.2.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    compileOnly(project(":server"))
    compileOnly(project(":sdk"))
    compileOnly("dev.hollowcube:minestom-ce:010fe985bb")
}

tasks.withType(ShadowJar::class.java) {
    dependencies {
        exclude {
            it.moduleGroup == "org.jetbrains.kotlin"
        }
    }
}

kotlin {
    jvmToolchain(19)
}

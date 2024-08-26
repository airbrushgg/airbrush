plugins {
    id("com.palantir.git-version") version "3.0.0"
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "dev.flavored"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly("net.minestom:minestom-snapshots:1f34e60ea6")
    testImplementation("net.minestom:minestom-snapshots:1f34e60ea6")
}

kotlin {
    jvmToolchain(21)
}
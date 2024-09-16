plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

val minestomVersion: String by extra { "4305006e6b" }

repositories {
    mavenCentral()
}

allprojects {
    apply(plugin = "kotlin")

    kotlin {
        jvmToolchain(21)
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    publishing {
        publications {
            create<MavenPublication>("maven") {
                groupId = "gg.airbrush"
                artifactId = project.name
                version = "${project.version}"

                from(components["java"])
            }
        }
    }
}
plugins {
    kotlin("jvm") version "2.0.0"
    `maven-publish`
}

group = "gg.airbrush"
version = "1.0.0"

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(21)
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
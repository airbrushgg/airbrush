plugins {
    kotlin("jvm") version "2.0.10"
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

    tasks.register<BuildAndPublishTask>("buildAndPublish") {
        group = "build"
        description = "Build and publish to Maven Local"

        dependsOn("build", "publishToMavenLocal")
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "maven-publish")

    publishing {
        afterEvaluate {
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

    java {
        withSourcesJar()
        withJavadocJar()
    }
}

abstract class BuildAndPublishTask : DefaultTask() {
    @TaskAction
    fun action() {}
}
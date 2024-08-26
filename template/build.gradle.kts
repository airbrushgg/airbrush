plugins {
    kotlin("jvm") version "2.0.0"
    application
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
    id("com.palantir.git-version") version "3.0.0"
}

val projectId: String by project
val projectName: String by project
val projectDescription: String by project

group = "gg.airbrush"
version = "0.1.0"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    compileOnly(project(":server")) // change to latest git hash
}

kotlin {
    jvmToolchain(19)
}

tasks.withType<ProcessResources> {
    val props = mapOf(
        "version" to version,
        "projectId" to projectId,
        "projectName" to projectName,
        "projectDescription" to projectDescription
    )

    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.toml") {
        expand(props)
    }
}

application {
    mainClass.set("MainKt")
}

val packageFile = File("src/main/kotlin/gg/airbrush/${projectId}")
val classFile = packageFile.resolve("$projectName.kt")

//if (!classFile.exists()) {
//    packageFile.mkdirs()
//
//    // this is probably awful but i hate renaming the packages and things
//    val indent = " ".repeat(4)
//    val code = StringBuilder()
//        .appendLine("package gg.airbrush.$projectId")
//        .appendLine()
//        .appendLine("import gg.airbrush.server.plugins.Plugin")
//        .appendLine()
//        .appendLine("class $projectName : Plugin() {")
//        .appendLine("${indent}override fun setup() {")
//        .appendLine("${indent.repeat(2)}// On start")
//        .appendLine("$indent}")
//        .appendLine()
//        .appendLine("${indent}override fun teardown() {")
//        .appendLine("${indent.repeat(2)}// On teardown")
//        .appendLine("$indent}")
//        .append("}")
//        .toString()
//
//    classFile.createNewFile()
//    classFile.writeText(code)
//}
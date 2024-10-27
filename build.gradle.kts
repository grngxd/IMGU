plugins {
    kotlin("jvm") version "2.0.0"
}

group = "cc.grng"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("com.github.SpaiR.imgui-java:imgui-java-app:v1.86.11")

    // Include devmodules only during development
    if (project.hasProperty("dev")) {
        implementation(project(":dev"))
    }
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
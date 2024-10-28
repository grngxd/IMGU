plugins {
    kotlin("jvm") version "2.0.0"
    id("maven-publish")
}

group = "cc.grng.imgu"
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

    compileOnly("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    testCompileOnly("org.projectlombok:lombok:1.18.28")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.28")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "cc.grng.imgu"
            artifactId = "IMGU"
            version = "1.0-SNAPSHOT"

            afterEvaluate {
                from(components["java"])
            }
        }
    }
}
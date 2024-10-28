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

    compileOnly("org.lwjgl:lwjgl:3.3.3")
    compileOnly("org.lwjgl:lwjgl-glfw:3.3.3")
    compileOnly("org.lwjgl:lwjgl-opengl:3.3.3")
    compileOnly("org.lwjgl:lwjgl-stb:3.3.3")
    compileOnly("org.lwjgl:lwjgl:3.3.3:natives-windows")
    compileOnly("org.lwjgl:lwjgl-glfw:3.3.3:natives-windows")
    compileOnly("org.lwjgl:lwjgl-opengl:3.3.3:natives-windows")
    compileOnly("org.lwjgl:lwjgl-stb:3.3.3:natives-windows")

    testImplementation("org.lwjgl:lwjgl:3.3.3")
    testImplementation("org.lwjgl:lwjgl-glfw:3.3.3")
    testImplementation("org.lwjgl:lwjgl-opengl:3.3.3")
    testImplementation("org.lwjgl:lwjgl-stb:3.3.3")
    testImplementation("org.lwjgl:lwjgl:3.3.3:natives-windows")
    testImplementation("org.lwjgl:lwjgl-glfw:3.3.3:natives-windows")
    testImplementation("org.lwjgl:lwjgl-opengl:3.3.3:natives-windows")
    testImplementation("org.lwjgl:lwjgl-stb:3.3.3:natives-windows")

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
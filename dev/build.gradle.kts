plugins {
    kotlin("jvm") version "2.0.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("org.lwjgl:lwjgl:3.3.1")
    implementation("org.lwjgl:lwjgl-glfw:3.3.1")
    implementation("org.lwjgl:lwjgl-opengl:3.3.1")
    implementation("org.lwjgl:lwjgl-stb:3.3.1")
    implementation("org.lwjgl:lwjgl:3.3.1:natives-windows")
    implementation("org.lwjgl:lwjgl-glfw:3.3.1:natives-windows")
    implementation("org.lwjgl:lwjgl-opengl:3.3.1:natives-windows")
    implementation("org.lwjgl:lwjgl-stb:3.3.1:natives-windows")
}
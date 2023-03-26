import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "me.kcybulski.ces"
    version = "0.0.1"

    repositories {
        mavenCentral()
    }

    dependencies {
        // Avoid the otherwise necessary "implementation"("…")
        val implementation by configurations
        val compileOnly by configurations
        val testImplementation by configurations

        // Production deps
        implementation("io.github.microutils:kotlin-logging:3.0.5")
        implementation("org.slf4j:slf4j-simple:2.0.6")

        // Test deps
        testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.5")
        testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.5")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    java.sourceCompatibility = org.gradle.api.JavaVersion.VERSION_17
    java.targetCompatibility = org.gradle.api.JavaVersion.VERSION_17
}
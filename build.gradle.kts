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
        implementation("io.github.microutils:kotlin-logging:3.0.4")
        compileOnly("org.slf4j:slf4j-simple:2.0.3")

        // Test deps
        testImplementation("io.kotest:kotest-runner-junit5-jvm:5.5.4")
        testImplementation("io.kotest:kotest-assertions-core-jvm:5.5.4")
    }

    tasks.getByName<Test>("test") {
        useJUnitPlatform()
    }
}
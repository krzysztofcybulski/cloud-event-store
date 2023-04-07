plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
    implementation("io.micrometer:micrometer-core:1.10.5")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.kcybulski.ces"
            artifactId = "event-store"
            from(components["java"])
        }
    }
}

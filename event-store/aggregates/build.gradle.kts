plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    api(project(":event-store"))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.10")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.kcybulski.ces"
            artifactId = "event-store-aggregates"
            from(components["java"])
        }
    }
}
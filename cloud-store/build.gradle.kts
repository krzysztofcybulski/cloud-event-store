plugins {
    kotlin("plugin.allopen") version "1.7.20"
    id("io.quarkus") version "2.13.1.Final"
}

dependencies {
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-bom:2.13.1.Final"))
    implementation(enforcedPlatform("io.quarkus.platform:quarkus-google-cloud-services-bom:2.13.1.Final"))
    implementation("io.quarkus:quarkus-grpc")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkiverse.googlecloudservices:quarkus-google-cloud-firestore")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-config-yaml")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0-rc3")
    testImplementation("io.quarkus:quarkus-junit5")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

allOpen {
    annotation("javax.ws.rs.Path")
    annotation("javax.enterprise.context.ApplicationScoped")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_17.toString()
    kotlinOptions.javaParameters = true
}

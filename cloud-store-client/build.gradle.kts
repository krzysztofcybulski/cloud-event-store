import com.google.protobuf.gradle.id

plugins {
    `java-library`
    `maven-publish`
    id("com.google.protobuf") version "0.9.2"
}

dependencies {
    api(project(":event-store"))
    implementation("io.grpc:grpc-kotlin-stub:1.3.0")
    implementation("io.grpc:grpc-protobuf:1.54.0")
    implementation("io.grpc:grpc-netty:1.54.0")
    implementation("com.google.protobuf:protobuf-kotlin:3.22.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.2")
}

sourceSets {
    main {
        proto {
            srcDir(project(":cloud-store-service").projectDir.resolve("src/main/proto"))
        }
        java {
        }
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.22.2"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.53.0"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:1.3.0:jdk8@jar"
        }
    }
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "me.kcybulski.ces"
            artifactId = "cloud-store-client"
            from(components["java"])
        }
    }
}

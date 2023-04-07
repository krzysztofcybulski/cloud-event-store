import com.google.protobuf.gradle.id

val grpcVersion by extra { "1.54.0" }
val grpcKotlinVersion by extra { "1.3.0" }
val protobufVersion by extra { "3.22.2" }
val nettyVersion by extra { "4.1.90.Final" }
val coroutinesVersion by extra { "1.7.0-Beta" }
val jacksonVersion by extra { "2.14.2" }

plugins {
    id("io.ratpack.ratpack-java") version "2.0.0-rc-1"
    id("com.google.protobuf") version "0.9.2"
}

dependencies {
    // Event Store
    implementation(project(":event-store"))
    implementation(project(":event-store:mongo-event-store"))
    implementation(project(":event-store:management"))
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    // Serialization
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    // GRPC
    implementation("io.grpc:grpc-netty:$grpcVersion")
    implementation("io.grpc:grpc-protobuf:$grpcVersion")
    implementation("io.grpc:grpc-services:$grpcVersion")
    implementation("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion")
    // Protobuf
    implementation("com.google.protobuf:protobuf-java-util:$protobufVersion")
    implementation("com.google.protobuf:protobuf-kotlin:$protobufVersion")
    // Netty
    implementation("io.netty:netty-transport-native-epoll:$nettyVersion")
    implementation("io.netty:netty-transport-native-kqueue:$nettyVersion")
    // Logging
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
    // Ratpack
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.5")
}

tasks.register("stage") {
    dependsOn("installDist")
}

application {
    mainClass.set("me.kcybulski.ces.service.StartKt")
    applicationDefaultJvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:$protobufVersion"
    }
    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcVersion"
        }
        id("grpckt") {
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
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

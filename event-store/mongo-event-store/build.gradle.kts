dependencies {
    implementation(project(":event-store"))
    implementation(project(":event-store:management"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.litote.kmongo:kmongo-coroutine:4.8.0")
}
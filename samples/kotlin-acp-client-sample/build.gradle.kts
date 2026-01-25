plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    implementation(project(":acp"))
    implementation(project(":acp-ktor"))
    implementation(project(":acp-ktor-client"))
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlin.logging)
    implementation(libs.kotlinx.io.core)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.websockets)
    implementation("io.ktor:ktor-client-cio:3.1.3")
    implementation("ch.qos.logback:logback-classic:1.5.13")
}

application {
    mainClass.set("com.agentclientprotocol.samples.client.ClientSampleKt")
}

kotlin {
    jvmToolchain(21)
}
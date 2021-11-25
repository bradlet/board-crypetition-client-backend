val ktorVersion: String by project
val kotlinVersion: String by project
val logbackVersion: String by project
val web3jVersion: String by project

plugins {
    application
    kotlin("jvm") version "1.6.0"
}

group = "com.bradlet"
version = "0.0.1"
application {
    mainClass.set("com.bradlet.ApplicationKt")
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor dependencies
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-gson:$ktorVersion")
    implementation("io.ktor:ktor-websockets:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // Blockchain dependencies
    implementation ("org.web3j:core:$web3jVersion")

    // Test dependencies
    testImplementation("io.ktor:ktor-server-tests:$ktorVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
}
rootProject.name = "board-crypetition"

pluginManagement {
    val kotlinVersion: String by settings
    val web3jVersion: String by settings
    val jibVersion: String by settings

    plugins {
        kotlin("jvm") version kotlinVersion
        id("org.web3j") version web3jVersion
        id ("com.google.cloud.tools.jib") version jibVersion
    }
}
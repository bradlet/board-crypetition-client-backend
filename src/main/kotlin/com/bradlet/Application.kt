package com.bradlet

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.bradlet.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        configureSecurity()
        configureHTTP()
        configureMonitoring()
        configureSerialization()
        configureSockets()
    }.start(wait = true)
}

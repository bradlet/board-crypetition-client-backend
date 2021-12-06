package com.bradlet

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.bradlet.plugins.*
import com.bradlet.routes.basePath
import com.bradlet.routes.game
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*

// Configure plugins and API routing
fun Application.mainApp() {
    /**
     * Configure web server plugins
     */
    configureHTTP()
    configureMonitoring()
    configureSockets()
    // Setup GSON json serialization / deserialization
    install(ContentNegotiation) { gson { } }

    /**
     * Setup API routes
     */
    routing {
        basePath()
        game()
    }
}

// Main entry point into our application
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::mainApp)
        .start(wait = true)
}

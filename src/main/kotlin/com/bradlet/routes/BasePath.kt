package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*

fun Route.basePath(client: EthereumClient) {
    get("/") {
        call.respondText("Hello World!")
    }
}

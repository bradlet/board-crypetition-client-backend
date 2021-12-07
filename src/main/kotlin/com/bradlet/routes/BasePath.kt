package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import com.google.gson.Gson
import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*

fun Route.basePath(client: EthereumClient) {
    get("/") {
        val lobbies = client.getAllGameLobbies()
        call.respondText(Gson().toJson(lobbies))
    }
}

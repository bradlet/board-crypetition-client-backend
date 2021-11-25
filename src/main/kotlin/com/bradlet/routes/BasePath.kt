package com.bradlet.routes

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*

fun Route.basePath() {
    get("/") {
        call.respondText("Hello World!")
    }
}

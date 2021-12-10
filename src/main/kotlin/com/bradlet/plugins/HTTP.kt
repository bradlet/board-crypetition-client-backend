package com.bradlet.plugins

import io.ktor.features.*
import io.ktor.application.*

fun Application.configureHTTP(redirect: Boolean) {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }

    if (redirect) {
        install(HttpsRedirect) {
            // The port to redirect to. By default 443, the default HTTPS port.
            sslPort = 443
            // 301 Moved Permanently, or 302 Found redirect.
            permanentRedirect = true
        }
    }
}

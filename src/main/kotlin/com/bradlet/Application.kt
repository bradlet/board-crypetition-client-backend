package com.bradlet

import com.bradlet.clients.EthereumClient
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.bradlet.plugins.*
import com.bradlet.routes.basePath
import com.bradlet.routes.game
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.routing.*
import okhttp3.OkHttpClient
import org.web3j.crypto.Credentials
import org.web3j.mycontract.MyContract
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider

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

    val ethHttpClient = HttpService(OkHttpClient())
    val ethClient = EthereumClient(
        contract = MyContract.load(
            "",
            Web3j.build(ethHttpClient),
            Credentials.create("private key here"),
            DefaultGasProvider()
        )
    )

    /**
     * Setup API routes
     */
    routing {
        basePath(ethClient)
        game(ethClient)
    }
}

// Main entry point into our application
fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::mainApp)
        .start(wait = true)
}

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
import org.web3j.boardcrypetition.BoardCrypetition
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider

// Main entry point into our application
fun main() {
    val ethHttpClient = HttpService(
        "https://ropsten.infura.io/v3/70ed7300192e4a9c86154a995ef9e925",
        OkHttpClient.Builder()
            // All calls via this client need authorization b/c I use Infura
            .addInterceptor { chain ->
                val authenticatedRequest = chain.request().newBuilder().addHeader(
                    "Authorization", "secret here eventually"
                ).build()
                chain.proceed(authenticatedRequest)
            }
            .build()
    )
    val ethClient = EthereumClient(
        contract = BoardCrypetition.load(
            "",
            Web3j.build(ethHttpClient),
            Credentials.create("0x01"),
            DefaultGasProvider()
        )
    )

    val mainApp = applicationEngineEnvironment {
        module {
            mainApp(ethClient)
        }

        connector {
            host = "0.0.0.0"
            port = 8080
        }
    }

    embeddedServer(Netty, mainApp).start(wait = true)
}

// Configure plugins and API routing
fun Application.mainApp(ethClient: EthereumClient) {
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
        basePath(ethClient)
        game(ethClient)
    }
}

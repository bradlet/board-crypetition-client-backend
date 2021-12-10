package com.bradlet

import io.ktor.client.*
import io.ktor.client.features.websocket.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    print("Please enter your wallet address before joining: ")
    val address = readLine() ?: return
    print("Please enter the ID of the game you'd like to join: ")
    val gameId = readLine() ?: return

    val client = HttpClient {
        install(WebSockets)
    }

    runBlocking {
        client.webSocket(
            HttpMethod.Get,
            "localhost",
            8080,
            "/game/$gameId"
        ) {
            val printIncomingJob = launch { printIncoming() }
            val sendUserInputJob = launch { sendUserInput(address) }

            sendUserInputJob.join()
            // Once user is done sending input, they should be done receiving, so:
            printIncomingJob.cancelAndJoin()
        }
    }
    client.close()
    println("KtoRC chat session has ended.")
}

private var stopCommunication = false

internal suspend fun DefaultClientWebSocketSession.printIncoming() {
    try {
        for (message in incoming) {
            if (message is Frame.Close) {
                stopCommunication = true
                return
            }
            println((message as Frame.Text).readText())
        }
    } catch (e: Exception) {
        stopCommunication = true
        println("Exception: ${e.message}")
    }
}

internal suspend fun DefaultClientWebSocketSession.sendUserInput(address: String) {
    do {
        try {
            readLine()?.let { msg ->

            }

            if (msg != null) {
                send(msg)
                if (msg == "bye")
                    return
            }

        } catch (e: Exception) {
            println(e.message)
            stopCommunication = true
        }
    } while (!stopCommunication)
}
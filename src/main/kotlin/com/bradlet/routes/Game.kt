package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*

fun Route.game(client: EthereumClient) {
    // websocket session for the main game's streaming communication
    webSocket("/game/{gameId}") {
        val lobby: GameLobby? = call.parameters["gameId"]?.let {
            client.findGameLobby(it)
        }

        lobby?.let {
            outgoing.send(
                Frame.Text(
                    "Joined lobby: ${it.gameId} [Game: ${
                        GameState.values().find { state -> state.stateCode == it.gameState}
                    }]"
                )
            )

        }

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(
                            CloseReason(
                                CloseReason.Codes.NORMAL,
                                "Client said BYE"
                            )
                        )
                    }
                }
                else -> {}
            }
        }
    }

}

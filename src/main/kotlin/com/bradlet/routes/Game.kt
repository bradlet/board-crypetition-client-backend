package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import com.bradlet.clients.toUint128
import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import org.web3j.abi.datatypes.generated.Uint128

fun Route.game(client: EthereumClient) {
    // websocket session for the main game's streaming communication
    webSocket("/game/{gameId}") {
        val lobby: GameLobby? = call.parameters["gameId"]?.let {
            client.findGameLobby(it.toUint128())
        }

        lobby?.let {
            outgoing.send(
                Frame.Text(
                    "Joined lobby: ${lobby.gameId} [Game: ${lobby.gameState}]"
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

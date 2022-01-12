package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import com.bradlet.clients.toUint128
import com.bradlet.models.GameLobby
import com.bradlet.models.StateChangeDeclaration
import com.bradlet.sendText
import com.google.gson.Gson
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import java.math.BigInteger

fun Route.game(client: EthereumClient) {
    // websocket session for the main game's streaming communication
    webSocket("/game/{gameId}") {
        val lobbyId: BigInteger? = call.parameters["gameId"]?.toUint128()
        val lobby: GameLobby = lobbyId?.let {
            client.findGameLobby(it)
        } ?: throw IllegalStateException("gameId must be provided.")

        outgoing.send(
            Frame.Text("Joined lobby: ${lobby.gameId} [Game: ${lobby.gameState}]")
        )

        for (frame in incoming) {
            when (frame) {
                is Frame.Text -> {
                    val text = frame.readText()

                    receiveStateUpdate(text)?.let { declaration ->
                        outgoing.sendText("Player ${declaration.playerAddress} has declared victory.")
                        try {
                            val response = client
                                .completeGame(lobbyId, lobby.players.first.value == declaration.playerAddress )
                            outgoing.sendText("Victory declaration transaction hash: $response")
                        } catch (e: IllegalStateException) {
                            outgoing.sendText("Failed to confirm victory: ${e.message}")
                        } catch (e: Exception) {
                            outgoing.sendText("Exception while sending declaration: ${e.message}")
                        }
                    }

                    if (text.contains("status", ignoreCase = true)) { // Find game state command
                        outgoing.sendText(
                            "Lobby $lobbyId is currently in state: ${client.findGameLobby(lobbyId).gameState}"
                        )
                    } else if ( // leave game commands
                        text.contains("bye", ignoreCase = true) ||
                        text.contains("exit", ignoreCase = true)
                    ) {
                        close(
                            CloseReason(CloseReason.Codes.NORMAL, "Client exited")
                        )
                    } else outgoing.sendText("Echo: $text") // Echo message sent otherwise.

                }
                else -> outgoing.sendText("Non text frame received")
            }
        }
    }

}

private fun receiveStateUpdate(input: String): StateChangeDeclaration? = try {
    Gson().fromJson(input, StateChangeDeclaration::class.java)
} catch (e: Exception) {
    null
}

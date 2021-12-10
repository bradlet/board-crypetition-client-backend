package com.bradlet.routes

import com.bradlet.clients.EthereumClient
import com.bradlet.clients.toUint128
import com.bradlet.models.GameLobby
import com.bradlet.models.StateChangeDeclaration
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
                    val declaration: StateChangeDeclaration? = try {
                        Gson().fromJson(text, StateChangeDeclaration::class.java)
                    } catch (e: Exception) {
                        null
                    }

                    /**
                     * For first iteration / example server behavior, a connected client can just declare victory.
                     */

                    declaration?.let {
                        outgoing.send(Frame.Text("Player ${it.playerAddress} has declared victory."))
                        try {
                            client.completeGame(lobbyId, lobby.players.first.value == it.playerAddress )
                        } catch (e: IllegalStateException) {
                            outgoing.send(Frame.Text("Failed to confirm victory."))
                        }
                    }
                    if (text.contains("bye", ignoreCase = true) || text.contains("exit", ignoreCase = true)) {
                        close(
                            CloseReason(CloseReason.Codes.NORMAL, "Client exited")
                        )
                    }
                }
                else -> {}
            }
        }
    }

}

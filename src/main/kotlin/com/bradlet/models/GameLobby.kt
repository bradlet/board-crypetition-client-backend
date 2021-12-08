package com.bradlet.models

import com.bradlet.SolLobbyTuple
import org.web3j.abi.datatypes.Address
import java.math.BigInteger

/**
 * Used to convey information on game state and membership to clients
 */
data class GameLobby(
    val gameId: BigInteger,
    val winnersPot: BigInteger,
    val players: Pair<Address, Address?>, // can't exist w/o at least a 1st player
    val gameState: GameState
) {
    companion object {
        fun of(web3jTuple: SolLobbyTuple): GameLobby {
            val (gameId, winnersPot, player1, player2, gameState) = web3jTuple
            return GameLobby(
                gameId = gameId,
                winnersPot = winnersPot,
                gameState = GameState.fromStateCode(gameState.toInt()),
                players = Address(player1) to Address(player2)
            )
        }
    }
}

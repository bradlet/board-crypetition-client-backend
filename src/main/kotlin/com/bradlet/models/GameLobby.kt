package com.bradlet.models

import org.web3j.abi.datatypes.Address
import org.web3j.tuples.generated.Tuple4
import java.math.BigInteger

/**
 * Used to convey information on game state and membership to clients
 */
data class GameLobby(
    val players: Pair<Address, Address?>, // can't exist w/o at least a 1st player
    val gameId: String,
    val gameState: Int
) {
    companion object {
        fun of(web3jTuple4: Tuple4<String, BigInteger, String, String>): GameLobby {
            val (gameId, gameState, player1, player2) = web3jTuple4
            return GameLobby(
                players = Address(player1) to Address(player2),
                gameId = gameId,
                gameState = gameState.toInt()
            )
        }
    }
}

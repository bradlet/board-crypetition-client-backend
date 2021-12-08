package com.bradlet.models

import com.bradlet.SolLobbyTuple
import org.web3j.abi.datatypes.Address
import java.math.BigInteger

/**
 * Used to convey information on game state and membership to clients
 */
data class GameLobby(
    val gameId: BigInteger, // uint128
    val wager: BigInteger, // uint256 -- wei
    val players: Pair<Address, Address?>, // can't exist w/o at least a 1st player
    val gameState: GameState // state code in uint8
) {
    companion object {
        fun of(web3jTuple: SolLobbyTuple): GameLobby {
            val (gameId, wager, player1, player2, gameState) = web3jTuple
            return GameLobby(
                gameId = gameId,
                wager = wager,
                gameState = GameState.fromStateCode(gameState.toInt()),
                players = Address(player1) to Address(player2)
            )
        }
    }
}

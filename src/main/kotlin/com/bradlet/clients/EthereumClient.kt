package com.bradlet.clients

import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import org.web3j.boardcrypetition.BoardCrypetition
import java.math.BigInteger

class EthereumClient(
    private val contract: BoardCrypetition
) {

    suspend fun completeGame(gameId: BigInteger, player1Won: Boolean) {
        val gameState = getGameState(gameId)
        if (gameState != GameState.READY)
            throw IllegalStateException(
                "Can't complete game with state: $gameState"
            )

        contract.completeGame(gameId, player1Won).send()
    }

    suspend fun getRecentOpenLobbies(): List<BigInteger> {
        // getRecentOpenLobbies return type: uint128[] memory
        return contract.recentOpenLobbies.send().map { it as BigInteger }
    }

    suspend fun findGameLobby(gameId: BigInteger) = GameLobby.of(contract.findGameLobby(gameId).send())

    private suspend fun getGameState(gameId: BigInteger): GameState {
        val gameStateCode = contract.lookupGameState(gameId).send().toInt()
        return GameState.fromStateCode(gameStateCode)
    }
}

// 16 digit hex string gives a uint128
fun String.toUint128(): BigInteger = BigInteger(this, 16)


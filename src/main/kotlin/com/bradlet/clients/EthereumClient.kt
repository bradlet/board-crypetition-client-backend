package com.bradlet.clients

import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import org.web3j.abi.datatypes.Address
import org.web3j.boardcrypetition.BoardCrypetition
import java.math.BigInteger

class EthereumClient(
    private val contract: BoardCrypetition
) {

    suspend fun completeGame(winner: Address, gameId: BigInteger) {
        val gameState = getGameState(gameId)
        if (gameState != GameState.READY)
            throw IllegalStateException(
                "Can't complete game with state: $gameState"
            )

        contract.finalizeGame(winner, gameId)
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

// Returns:
//  | true for games that, after call to finalizeGame, return COMPLETED from
//  |   checkGameState as expected.
//  | false for games that, after call to finalizeGame, return ERRORED or other state
//  |   from checkGameState.
private fun BoardCrypetition.finalizeGame(winningPlayer: Address, gameId: BigInteger): Boolean {
    // When game server determines that a player has won, send ID of game session,
    // and winningPlayer's address to kickoff payout and stateCode change for game mapping
    // Games cannot be reinitialized, once a game is COMPLETED it can never be altered.
    return true
}

// 16 digit hex string gives a uint128
fun String.toUint128(): BigInteger = BigInteger(this, 16)


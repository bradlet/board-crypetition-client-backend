package com.bradlet.clients

import com.bradlet.SolLobbyTuple
import com.bradlet.TEMPORARY_EXAMPLE_LOBBY_LIST
import com.bradlet.TEMPORARY_GAME_LOBBY_SOL
import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import com.fasterxml.jackson.databind.deser.std.NumberDeserializers
import org.web3j.abi.datatypes.Address
import org.web3j.boardcrypetition.BoardCrypetition
import java.math.BigInteger
import java.util.*

class EthereumClient(
    private val contract: BoardCrypetition
) {

    suspend fun initializeGame(creator: Address): BigInteger {
        return contract.createGame(creator)
    }

    suspend fun addPlayerToGame(newPlayer: Address, gameId: BigInteger): GameState {
        val gameState = getGameState(gameId)
        if (gameState != GameState.INITIALIZED)
            return gameState

        contract.startGame(newPlayer, gameId)
        return getGameState(gameId) // Return post-startup game state so clients can know if game started successfully
    }

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

/**
 * For simplicity's sake, going to emulate contract interactions here until
 * the real contract can be built via web3j.
 * In each fun, going to plan out the steps-to-completion including functionality
 * that won't be hosted in these contract interactions necessarily.
 */
private fun BoardCrypetition.createGame(creator: Address): BigInteger {
    val uuid = UUID.randomUUID().toString()
    println(uuid)
    return BigInteger(uuid, 16) // uint128 has 16 bytes in a hex string
}

private fun BoardCrypetition.startGame(
    opponent: Address,
    gameId: BigInteger
){
    // 1. Receive two addresses for players.
    // 2. Create game ID --> game ID == session ID that will be used to join WebSocket
    //  game session.
    // 3. start game and echo game Id for verification
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


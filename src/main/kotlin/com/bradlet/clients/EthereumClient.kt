package com.bradlet.clients

import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import org.web3j.abi.datatypes.Address
import org.web3j.mycontract.MyContract
import java.math.BigInteger
import java.util.*

class EthereumClient(
    private val contract: MyContract
) {

    fun initializeGame(creator: Address): String {
        return contract.createGame(creator)
    }

    fun addPlayerToGame(newPlayer: Address, gameId: String): GameState {
        val stateOfProvidedGame = contract.checkGameState(gameId)
        if (stateOfProvidedGame != GameState.INITIALIZED)
            return stateOfProvidedGame

        contract.startGame(newPlayer, gameId)
        return contract.checkGameState(gameId)
    }

    fun completeGame(winner: Address, gameId: String) {
        val stateOfProvidedGame = contract.checkGameState(gameId)
        if (stateOfProvidedGame != GameState.READY)
            throw IllegalStateException(
                "Can't complete game with state: ${stateOfProvidedGame.name}"
            )

        contract.finalizeGame(winner, gameId)
    }
}

/**
 * For simplicity's sake, going to emulate contract interactions here until
 * the real contract can be built via web3j.
 * In each fun, going to plan out the steps-to-completion including functionality
 * that won't be hosted in these contract interactions necessarily.
 */
private fun MyContract.createGame(creator: Address): String {
    return UUID.randomUUID().toString()
}

private fun MyContract.startGame(
    opponent: Address,
    gameId: String
){
    // 1. Receive two addresses for players.
    // 2. Create game ID --> game ID == session ID that will be used to join WebSocket
    //  game session.
    // 3. start game and echo game Id for verification
}

// Return from contract would be returns(string, uint6, string, string) for gameId, gameState, player1, player2
private fun MyContract.findGameLobby(gameId: String): GameLobby {
    // I'd want a dynamic array of structs, which would essentially match the GameLobby object described in this
    // service.
    // Would want an array of INITIALIZED games for users to join.

    // Adding this example of reading from a view function that would return a tuple of all the info in a
    // game lobby struct. GameLobby.of() shows the conversion of the web3j Tuple4 type, to local GameLobby object.
//    return GameLobby.of(quadruple.send())
    return GameLobby(players = Address(BigInteger.ZERO) to Address(BigInteger.ZERO), gameId = "1", 2)
}

private fun MyContract.checkGameState(gameId: String): GameState {
    // Used to check if the game has been completed in the blockchain and payouts
    // should be sent.
    // 1. Call smart contract view function to read stateCode from gameId -> stateCode
    //  mapping.
    return GameState.NOT_INITIALIZED
}

val TEMPORARY_EXAMPLE_LOBBY_LIST = listOf("1", "2", "3", "4")
private fun MyContract.getOpenLobbies(): List<String> {
    // Will implement something like a LinkedList that will sit overtop of Solidity arrays in the contract.
    // Just need some mechanism for conveying gameIds that can be joined by new users. Custom list may be overkill.
    return TEMPORARY_EXAMPLE_LOBBY_LIST
}

// Returns:
//  | true for games that, after call to finalizeGame, return COMPLETED from
//  |   checkGameState as expected.
//  | false for games that, after call to finalizeGame, return ERRORED or other state
//  |   from checkGameState.
private fun MyContract.finalizeGame(winningPlayer: Address, gameId: String): Boolean {
    // When game server determines that a player has won, send ID of game session,
    // and winningPlayer's address to kickoff payout and stateCode change for game mapping
    // Games cannot be reinitialized, once a game is COMPLETED it can never be altered.
    return true
}


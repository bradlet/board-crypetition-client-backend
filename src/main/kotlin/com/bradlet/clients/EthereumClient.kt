package com.bradlet.clients

import com.bradlet.models.GameState
import org.web3j.abi.datatypes.Address
import org.web3j.mycontract.MyContract
import java.util.*

class EthereumClient(
    val contract: MyContract
) {

    fun initiateGame() {
    }
}

/**
 * For simplicity's sake, going to emulate contract interactions here until
 * the real contract can be built via web3j.
 * In each fun, going to plan out the steps-to-completion including functionality
 * that won't be hosted in these contract interactions necessarily.
 */
private fun MyContract.startGame(
    player1: Address,
    player2: Address,
    gameId: String
): String {
    // 1. Receive two addresses for players.
    // 2. Create game ID --> game ID == session ID that will be used to join WebSocket
    //  game session.
    // 3. start game and echo game Id for verification
    val id = UUID.randomUUID().toString()
    println(id)
    return id
}

private fun MyContract.checkGameState(gameId: String): GameState {
    // Used to check if the game has been completed in the blockchain and payouts
    // should be sent.
    // 1. Call smart contract view function to read stateCode from gameId -> stateCode
    //  mapping.

    return GameState.NOT_INITIALIZED
}

// Returns:
//  | true for games that, after call to finalizeGame, return COMPLETED from
//  |   checkGameState as expected.
//  | false for games that, after call to finalizeGame, return ERRORED or other state
//  |   from checkGameState.
private fun MyContract.finalizeGame(gameId: String, winningPlayer: Address): Boolean {
    // When game server determines that a player has won, send ID of game session,
    // and winningPlayer's address to kickoff payout and stateCode change for game mapping
    // Games cannot be reinitialized, once a game is COMPLETED it can never be altered.
    return true
}


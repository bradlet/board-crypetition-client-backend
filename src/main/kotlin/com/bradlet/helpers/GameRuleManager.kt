package com.bradlet.helpers

/**
 * Each different type of supported game must have a rule manager that supports commonly used functionality.
 */
interface GameRuleManager {

    // Takes a game state string, as well as the piece that is being considered to have won the game.
    // Returns true if the player with targetPiece has won, false if they have not.
    fun checkVictory(state: String, targetPiece: Char): Boolean

    // Should take the previously recorded game state (which should have been held onto by the server for a given
    // game lobby) and compares it to the state after some move has been made, to determine if the change was possible.
    fun checkValidMove(previousState: String, proposedState: String): Boolean
}
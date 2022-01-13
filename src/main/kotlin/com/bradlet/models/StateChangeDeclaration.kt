package com.bradlet.models

// The primary communication model used by clients to inform the server of changes in state.
data class StateChangeDeclaration(
    val playerAddress: String,
    val declaration: Declaration,
    val gameState: String = "", // Will be a string which will represent the board state for any supported game.
    val gameType: SupportedGame = SupportedGame.TIK_TAC_TOE
)

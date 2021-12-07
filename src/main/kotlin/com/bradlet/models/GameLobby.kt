package com.bradlet.models

import org.web3j.abi.datatypes.Address

/**
 * Used to convey information on game state and membership to clients
 */
data class GameLobby(
    val players: Pair<Address, Address?>, // can't exist w/o at least a 1st player
    val gameId: String,
    val gameState: String
)

package com.bradlet.models

enum class GameState(val stateCode: Int) {
    NOT_INITIALIZED(0), // No game Id exists in mapping
    INITIALIZED(1), // game Id exists in mapping, but doesn't have 2 players
    READY(2), // game Id exists in mapping and has 2 players
    COMPLETED(3); // game finished and ready for payout

    companion object {
        fun fromStateCode(code: Int): GameState = values().find { it.stateCode == code } ?: NOT_INITIALIZED
    }
}

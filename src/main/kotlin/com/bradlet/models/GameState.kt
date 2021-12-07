package com.bradlet.models

enum class GameState(val stateCode: Int) {
    NOT_INITIALIZED(0), // No game Id exists in mapping
    INITIALIZED(1), // game Id exists in mapping, but doesn't have 2 players
    READY(2), // game Id exists in mapping and has 2 players
    COMPLETED(3), // game finished and ready for payout
    PAID_OUT(4), // game finished and payout has been sent
    ERRORED(5); // an error occurred that prevents the game from completing
}

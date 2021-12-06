package com.bradlet.models

enum class GameState(stateCode: Int) {
    NOT_INITIALIZED(0),
    INITIALIZED(1),
    COMPLETED(2),
    ERRORED(3);
}

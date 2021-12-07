package com.bradlet

import com.bradlet.models.GameLobby
import org.web3j.tuples.generated.Tuple4
import java.math.BigInteger

val TEMPORARY_EXAMPLE_LOBBY_LIST = listOf("1", "2", "3", "4")
val TEMPORARY_GAME_LOBBY_SOL = Tuple4(
    "1",
    BigInteger.ONE,
    "0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b",
    "0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b"
)
val TEMPORARY_GAME_LOBBY = GameLobby.of(TEMPORARY_GAME_LOBBY_SOL)

package com.bradlet

import com.bradlet.models.GameLobby
import org.web3j.tuples.generated.Tuple5
import java.math.BigInteger

const val AN_ADDRESS = "0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b"

val TEMPORARY_EXAMPLE_LOBBY_LIST = (1L..4L).map { BigInteger.valueOf(it) }
val TEMPORARY_GAME_LOBBY_SOL = Tuple5(
    BigInteger.ONE,
    BigInteger.ONE,
    AN_ADDRESS,
    AN_ADDRESS,
    BigInteger.ONE
)
val TEMPORARY_GAME_LOBBY = GameLobby.of(TEMPORARY_GAME_LOBBY_SOL)


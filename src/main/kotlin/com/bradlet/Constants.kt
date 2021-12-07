package com.bradlet

import com.bradlet.models.GameLobby
import org.web3j.abi.datatypes.Address

val TEMPORARY_EXAMPLE_LOBBY_LIST = listOf("1", "2", "3", "4")
val TEMPORARY_GAME_LOBBY = GameLobby(
    players = Pair(
        Address("0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b"),
        Address("0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b")
    ),
    gameId = "1",
    2
)

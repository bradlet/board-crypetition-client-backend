package com.bradlet

import org.web3j.tuples.generated.Tuple5
import java.math.BigInteger

// gameId, wager, player1, player2, gameState
typealias SolLobbyTuple = Tuple5<BigInteger, BigInteger, String, String, BigInteger>

const val ROPSTEN_CHAIN_ID = 3L

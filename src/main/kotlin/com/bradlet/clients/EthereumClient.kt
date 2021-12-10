package com.bradlet.clients

import com.bradlet.CONTRACT_ADDR
import com.bradlet.ROPSTEN_CHAIN_ID
import com.bradlet.models.GameLobby
import com.bradlet.models.GameState
import org.web3j.boardcrypetition.BoardCrypetition
import org.web3j.crypto.RawTransaction
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.tx.RawTransactionManager
import org.web3j.tx.gas.StaticGasProvider
import java.math.BigInteger

class EthereumClient(
    private val contract: BoardCrypetition,
    private val web3j: Web3j,
    private val provider: StaticGasProvider,
    private val txnManager: RawTransactionManager
) {

    /**
     * Web3J doesn't support sending transactions that update state, signed, via contract wrapper.
     * So, we need to create a raw transaction, using the contract wrapper to encode our function call.
     */
    suspend fun completeGame(gameId: BigInteger, player1Won: Boolean): String {
        val gameState = getGameState(gameId)
        if (gameState != GameState.READY)
            throw IllegalStateException("Can't complete game with state <$gameState>")

        try {
            val callData = contract.completeGame(gameId, player1Won).encodeFunctionCall()
            val nonce = web3j
                .ethGetTransactionCount(txnManager.fromAddress, DefaultBlockParameterName.LATEST)
                .send().transactionCount
            val txn = RawTransaction.createTransaction(
                ROPSTEN_CHAIN_ID,
                nonce,
                provider.gasLimit,
                CONTRACT_ADDR,
                BigInteger.ZERO,
                callData,
                provider.gasPrice,
                provider.gasPrice
            )

            val ethTxn = txnManager.signAndSend(txn)
            if (ethTxn.hasError())
                return ethTxn.error.message

            return ethTxn.result
        } catch (e: Exception) {
            throw IllegalStateException("Complete game transaction returned error. ${e.message}")
        }
    }

    suspend fun getRecentOpenLobbies(): List<BigInteger> {
        // getRecentOpenLobbies return type: uint128[] memory
        return contract.recentOpenLobbies.send().map { it as BigInteger }
    }

    suspend fun findGameLobby(gameId: BigInteger) = GameLobby.of(contract.findGameLobby(gameId).send())

    private suspend fun getGameState(gameId: BigInteger): GameState {
        val gameStateCode = contract.lookupGameState(gameId).send().toInt()
        return GameState.fromStateCode(gameStateCode)
    }
}

// 16 digit hex string gives a uint128
fun String.toUint128(): BigInteger = BigInteger(this, 16)


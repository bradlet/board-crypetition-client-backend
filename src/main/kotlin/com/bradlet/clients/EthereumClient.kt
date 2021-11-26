package com.bradlet.clients

import org.web3j.abi.datatypes.Function
import org.web3j.mycontract.MyContract

class EthereumClient(
    val contract: MyContract
) {

    fun initiateGame() {
        val function = Function(
            "functionNameHere",
            emptyList(), // input params
            emptyList(), // output params
        )
    }
}
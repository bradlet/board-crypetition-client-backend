package com.bradlet.models

// Structure that will be more meaningful later, for now just used to exemplify client-server communication.
data class StateChangeDeclaration(
    val playerAddress: String,
    val declaration: Declaration
)

enum class Declaration {
    WON;
}

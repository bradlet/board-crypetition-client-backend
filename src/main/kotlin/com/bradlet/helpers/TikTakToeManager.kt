package com.bradlet.helpers

class TikTacToeManager: GameRuleManager {

    companion object {
        const val xPiece = 'X'
        const val oPiece = 'O'

        internal fun buildGameMatrix(input: String): Array<String> {
            if (input.length != 9) throw IllegalStateException(
                "Invalid tik tac toe state string: Incorrect num of chars."
            )

            return arrayOf(
                input.slice(0..2),
                input.slice(3..5),
                input.slice(6..8)
            )
        }
    }

    override fun checkVictory(state: String, targetPiece: Char): Boolean {
        val otherPiece = if (targetPiece == xPiece) oPiece else xPiece


        return false
    }

    override fun checkValidMove(previousState: String, proposedState: String): Boolean {
        TODO("Not yet implemented")
    }

}
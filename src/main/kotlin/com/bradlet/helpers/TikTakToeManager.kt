package com.bradlet.helpers

class TikTacToeManager: GameRuleManager {

    override fun checkVictory(state: String, targetPiece: Char): Boolean {
        val otherPiece = if (targetPiece == xPiece) oPiece else xPiece
        val matrix = buildGameMatrix(state)
        // First make sure the other piece didn't win already
        // TODO: Maybe remove this if this is never a possible case
        if (pieceDidWin(matrix, otherPiece)) return false

        return pieceDidWin(matrix, targetPiece)
    }

    override fun checkValidMove(previousState: String, proposedState: String): Boolean {
        TODO("Not yet implemented")
    }

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

        // Really this holds all the game win rules for tik-tac-toe
        internal fun pieceDidWin(matrix: Array<String>, targetPiece: Char): Boolean {
            fun targetCharsEqual(first: Char, second: Char, third: Char): Boolean =
                first == targetPiece && first == second && first == third

            // Horizontals
            matrix.forEach { row -> if (targetCharsEqual(row[0], row[1], row[2])) return true }

            // Verticals
            (0..2).forEach { i -> if (targetCharsEqual(matrix[0][i], matrix[1][i], matrix[2][i])) return true }

            // Diagonals
            if (targetCharsEqual(matrix[0][0], matrix[1][1], matrix[2][2])) return true
            if (targetCharsEqual(matrix[0][2], matrix[1][1], matrix[2][0])) return true

            // If victory hasn't been found yet, then return false
            return false
        }
    }
}
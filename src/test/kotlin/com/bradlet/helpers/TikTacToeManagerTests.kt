package com.bradlet.helpers

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TikTacToeManagerTests {

    @Test
    fun `buildGameMatrix builds 3 by 3 game matrix correctly`() {
        val testString = "XXXOXOOXO"

        val matrix = TikTacToeManager.buildGameMatrix(testString)
        assert(matrix.size == 3)
        matrix.forEach { assert(it.length == 3) }
    }

    @Test
    fun `buildGameMatrix throws IllegalStateException if input string is invalid`() {
        val testString = "tooShort"

        assertThrows<IllegalStateException> {
            TikTacToeManager.buildGameMatrix(testString)
        }
    }
}
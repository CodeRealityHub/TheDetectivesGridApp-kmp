package com.example.thedetectivesgrid.generator

import com.example.thedetectivesgrid.models.PuzzleData
import com.example.thedetectivesgrid.models.WordPlacement
import kotlin.random.Random

object PuzzleGenerator {

    private const val GRID_SIZE = 9

    private val ALL_DIRECTIONS = listOf(
        Pair(0,  1),  // Horizontal forward
        Pair(1,  0),  // Vertical forward
        Pair(1,  1),  // Diagonal down-right
        Pair(0, -1),  // Horizontal backward
        Pair(-1, 0),  // Vertical backward
        Pair(-1,-1),  // Diagonal up-left
        Pair(1, -1),  // Diagonal down-left
        Pair(-1, 1)   // Diagonal up-right
    )

    fun generate(words: List<String>): PuzzleData {

        val grid = Array(GRID_SIZE) { CharArray(GRID_SIZE) { ' ' } }
        val placements = mutableListOf<WordPlacement>()

        val sortedWords = words
            .map { it.uppercase() }
            .sortedByDescending { it.length }

        sortedWords.forEachIndexed { index, word ->

            val direction = when (index % 3) {
                0    -> Pair(0, 1)
                1    -> Pair(1, 0)
                else -> Pair(1, 1)
            }

            val placed = placeWord(grid, word, direction.first, direction.second, placements)

            if (!placed) {
                // Retry with all 8 directions
                var success = false
                for (dir in ALL_DIRECTIONS.shuffled()) {
                    if (placeWord(grid, word, dir.first, dir.second, placements)) {
                        success = true
                        break
                    }
                }
                // Last resort: force place so word is always findable
                if (!success) {
                    forcePlaceWord(grid, word, placements)
                }
            }
        }

        fillEmptyCells(grid)

        return PuzzleData(
            grid = grid.flatMap { row -> row.map { it.toString() } },
            placements = placements
        )
    }

    // ── NEW: guaranteed placement as last resort ──────────────
    private fun forcePlaceWord(
        grid: Array<CharArray>,
        word: String,
        placements: MutableList<WordPlacement>
    ) {
        // Scan every horizontal slot first, then vertical
        val directions = listOf(Pair(0, 1), Pair(1, 0))
        for ((rs, cs) in directions) {
            for (row in 0 until GRID_SIZE) {
                for (col in 0 until GRID_SIZE) {
                    if (canPlaceWord(grid, word, row, col, rs, cs)) {
                        val positions = writeWord(grid, word, row, col, rs, cs)
                        placements.add(WordPlacement(word = word, positions = positions))
                        return
                    }
                }
            }
        }
        println("CRITICAL: Could not place even with force: $word")
    }

    private fun placeWord(
        grid: Array<CharArray>,
        word: String,
        rowStep: Int,
        colStep: Int,
        placements: MutableList<WordPlacement>
    ): Boolean {

        // ── OVERLAP PASS ──────────────────────────────────────
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {

                if (!canPlaceWord(grid, word, row, col, rowStep, colStep)) continue

                var overlapCount = 0
                for (i in word.indices) {
                    val r = row + i * rowStep
                    val c = col + i * colStep
                    if (grid[r][c] != ' ' && grid[r][c] == word[i]) overlapCount++
                }

                if (overlapCount > 0) {
                    val positions = writeWord(grid, word, row, col, rowStep, colStep)
                    placements.add(WordPlacement(word = word, positions = positions))
                    return true
                }
            }
        }

        // ── RANDOM PASS — constrained ranges so start is always valid ──
        val rowMin = if (rowStep < 0) word.length - 1 else 0
        val rowMax = if (rowStep > 0) GRID_SIZE - word.length else GRID_SIZE - 1
        val colMin = if (colStep < 0) word.length - 1 else 0
        val colMax = if (colStep > 0) GRID_SIZE - word.length else GRID_SIZE - 1

        if (rowMin > rowMax || colMin > colMax) return false

        repeat(500) {
            val row = Random.nextInt(rowMin, rowMax + 1)
            val col = Random.nextInt(colMin, colMax + 1)

            if (canPlaceWord(grid, word, row, col, rowStep, colStep)) {
                val positions = writeWord(grid, word, row, col, rowStep, colStep)
                placements.add(WordPlacement(word = word, positions = positions))
                return true
            }
        }

        return false
    }

    // ── FIXED: handles all 8 directions including negative steps ──
    private fun canPlaceWord(
        grid: Array<CharArray>,
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ): Boolean {
        for (i in word.indices) {
            val row = startRow + i * rowStep
            val col = startCol + i * colStep

            // ✅ checks both < 0 and >= GRID_SIZE
            if (row < 0 || row >= GRID_SIZE || col < 0 || col >= GRID_SIZE) return false

            val existing = grid[row][col]
            if (existing != ' ' && existing != word[i]) return false
        }
        return true
    }

    private fun writeWord(
        grid: Array<CharArray>,
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ): List<Int> {
        val positions = mutableListOf<Int>()
        for (i in word.indices) {
            val row = startRow + i * rowStep
            val col = startCol + i * colStep
            grid[row][col] = word[i]
            positions.add(row * GRID_SIZE + col)
        }
        return positions
    }

    private fun fillEmptyCells(grid: Array<CharArray>) {
        for (row in 0 until GRID_SIZE) {
            for (col in 0 until GRID_SIZE) {
                if (grid[row][col] == ' ') {
                    grid[row][col] = ('A'.code + Random.nextInt(26)).toChar()
                }
            }
        }
    }
}
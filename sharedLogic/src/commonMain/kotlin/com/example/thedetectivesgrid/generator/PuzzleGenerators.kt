package com.example.thedetectivesgrid.generator
//  android
import com.example.thedetectivesgrid.models.PuzzleData
import com.example.thedetectivesgrid.models.WordPlacement
import kotlin.math.ceil
import kotlin.math.sqrt
import kotlin.random.Random

object PuzzleGenerators {
    private val ALL_DIRECTIONS = listOf(
        Pair(0, 1),
        Pair(0, -1),
        Pair(1, 0),
        Pair(-1, 0),
        Pair(1, 1),
        Pair(-1, -1),
        Pair(1, -1),
        Pair(-1, 1)
    )

    private fun calculateGridSize(words: List<String>) : Int {
        val totalLetters = words.sumOf { it.length }
        val longestWord = words.maxOf { it.length }
        val size = ceil(sqrt(totalLetters * 2.0)).toInt()
        println("GRID_DEBUG total=$totalLetters longest=$longestWord size=$size return=${maxOf(size, longestWord)}")
        return maxOf(size, longestWord)
    }

    fun generate(words: List<String>): PuzzleData {
        val gridSize = calculateGridSize(words)
        val grid = Array(gridSize) { CharArray(gridSize) { ' ' } }
        val placements = mutableListOf<WordPlacement>()

        val sortedWords = words.map { it.uppercase() }
            .sortedByDescending { it.length }

        sortedWords.forEach { word ->

            var placed = false

            // -------------------------------
            // PASS 1: Smart randomized placement
            // -------------------------------
            val dirs = ALL_DIRECTIONS.shuffled()

            for (dir in dirs) {
                if (placeWord(grid,  gridSize, word, dir.first, dir.second, placements)) {
                    placed = true
                    break
                }
            }

            // -------------------------------
            // PASS 2: Full grid brute scan
            // -------------------------------
            if (!placed) {
                outer@ for (r in 0 until gridSize) {
                    for (c in 0 until gridSize) {
                        for (dir in ALL_DIRECTIONS) {

                            if (canPlaceWord(grid,  gridSize, word, r, c, dir.first, dir.second)) {

                                val positions = writeWord(
                                    grid,
                                    gridSize,
                                    word,
                                    r,
                                    c,
                                    dir.first,
                                    dir.second
                                )

                                placements.add(
                                    WordPlacement(word, positions)
                                )

                                placed = true
                                break@outer
                            }
                        }
                    }
                }
            }

            // -------------------------------
            // PASS 3: Hard fallback (horizontal only)
            // -------------------------------
            if (!placed) {
                for (r in 0 until gridSize) {
                    for (c in 0..gridSize - word.length) {

                        if (canPlaceWord(grid,  gridSize, word, r, c, 0, 1)) {

                            val positions = writeWord(
                                grid,
                                gridSize,
                                word,
                                r,
                                c,
                                0,
                                1
                            )

                            placements.add(
                                WordPlacement(word, positions)
                            )

                            placed = true
                            break
                        }
                    }
                    if (placed) break
                }
            }

            if (!placed) {
                throw IllegalStateException("FAILED TO PLACE WORD: $word")
            }
        }

        fillEmptyCells(grid,  gridSize)

        return PuzzleData(
            grid = grid.flatMap { row -> row.map { it.toString() } },
            placements = placements,
            gridSize = gridSize
        )
    }

    private fun placeWord(
        grid: Array<CharArray>,
        gridSize: Int,
        word: String,
        rowStep: Int,
        colStep: Int,
        placements: MutableList<WordPlacement>
    ): Boolean {

        var bestRow = -1
        var bestCol = -1
        var bestOverlap = -1

        // -------------------------------
        // FIND BEST POSITION
        // -------------------------------
        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {

                if (!canPlaceWord(grid,  gridSize, word, r, c, rowStep, colStep)) continue

                var overlap = 0

                for (i in word.indices) {
                    val rr = r + i * rowStep
                    val cc = c + i * colStep

                    if (grid[rr][cc] == word[i]) {
                        overlap++
                    }
                }

                if (overlap > bestOverlap) {
                    bestOverlap = overlap
                    bestRow = r
                    bestCol = c
                }
            }
        }

        // -------------------------------
        // SAFE CHECK (CRITICAL FIX)
        // -------------------------------
        if (bestRow != -1) {

            val positions = writeWord(
                grid,
                gridSize,
                word,
                bestRow,
                bestCol,
                rowStep,
                colStep
            )

            placements.add(WordPlacement(word, positions))
            return true
        }

        // -------------------------------
        // RANDOM FALLBACK
        // -------------------------------
        val rowMin = if (rowStep < 0) word.length - 1 else 0
        val rowMax = if (rowStep > 0) gridSize - word.length else gridSize - 1

        val colMin = if (colStep < 0) word.length - 1 else 0
        val colMax = if (colStep > 0) gridSize - word.length else gridSize - 1

        if (rowMin > rowMax || colMin > colMax) return false

        repeat(300) {

            val r = Random.nextInt(rowMin, rowMax + 1)
            val c = Random.nextInt(colMin, colMax + 1)

            if (canPlaceWord(grid,  gridSize, word, r, c, rowStep, colStep)) {

                val positions = writeWord(
                    grid,
                    gridSize,
                    word,
                    r,
                    c,
                    rowStep,
                    colStep
                )

                placements.add(WordPlacement(word, positions))
                return true
            }
        }

        return false
    }
    // -------------------------------
    // VALIDATION
    // -------------------------------
    private fun canPlaceWord(
        grid: Array<CharArray>,
        gridSize: Int,
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ): Boolean {

        for (i in word.indices) {

            val r = startRow + i * rowStep
            val c = startCol + i * colStep

            if (r !in 0 until gridSize || c !in 0 until gridSize) {
                return false
            }

            val existing = grid[r][c]
            if (existing != ' ' && existing != word[i]) {
                return false
            }
        }

        return true
    }

    // -------------------------------
    // WRITE WORD
    // -------------------------------
    private fun writeWord(
        grid: Array<CharArray>,
        gridSize: Int,
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ): List<Int> {

        val positions = mutableListOf<Int>()

        for (i in word.indices) {

            val r = startRow + i * rowStep
            val c = startCol + i * colStep

            grid[r][c] = word[i]
            positions.add(r * gridSize + c)
        }

        return positions
    }

    // -------------------------------
    // FILL EMPTY GRID
    // -------------------------------
    private fun fillEmptyCells(
        grid: Array<CharArray>,
        gridSize: Int
    ) {

        for (r in 0 until gridSize) {
            for (c in 0 until gridSize) {

                if (grid[r][c] == ' ') {
                    grid[r][c] = ('A' + Random.nextInt(26))
                }
            }
        }
    }
}
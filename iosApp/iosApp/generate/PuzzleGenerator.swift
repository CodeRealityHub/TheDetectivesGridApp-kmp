//
//  PuzzleGenerator.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

final class PuzzleGenerator {

    private let directions: [(row: Int, col: Int)] = [
        (0, 1), (0, -1),
        (1, 0), (-1, 0),
        (1, 1), (-1, -1),
        (1, -1), (-1, 1)
    ]

    // MARK: - Dynamic grid size based on word count / length
    private func calculateGridSize(words: [String]) -> Int {
        let totalLetters = words.reduce(0) { $0 + $1.count }
        let longestWord   = words.map { $0.count }.max() ?? 0
        let size = Int(ceil(sqrt(Double(totalLetters) * 2.0)))
        return max(size, longestWord)
    }

    func generate(words: [String]) -> PuzzleData? {

        guard !words.isEmpty else { return nil }

        let size = calculateGridSize(words: words)

        var grid = Array(repeating: " ", count: size * size)
        var placements: [WordPlacement] = []

        let sortedWords = words
            .map { $0.uppercased() }
            .sorted { $0.count > $1.count }

        for word in sortedWords {

            guard word.count <= size else { continue }

            var placed = false

            // Pass 1: randomized direction order, best-overlap placement
            for dir in directions.shuffled() {
                if placeWord(word: word, dir: dir, size: size, grid: &grid, placements: &placements) {
                    placed = true
                    break
                }
            }

            // Pass 2: full grid brute scan across all directions
            if !placed {
                outer: for row in 0..<size {
                    for col in 0..<size {
                        for dir in directions {
                            if canPlaceWord(word: word, row: row, col: col, dir: dir, size: size, grid: grid) {
                                let positions = writeWord(word: word, row: row, col: col, dir: dir, size: size, grid: &grid)
                                placements.append(WordPlacement(word: word, positions: positions))
                                placed = true
                                break outer
                            }
                        }
                    }
                }
            }

            // Pass 3: hard fallback, horizontal only
            if !placed {
                rowLoop: for row in 0..<size {
                    for col in 0...(size - word.count) {
                        if canPlaceWord(word: word, row: row, col: col, dir: (0, 1), size: size, grid: grid) {
                            let positions = writeWord(word: word, row: row, col: col, dir: (0, 1), size: size, grid: &grid)
                            placements.append(WordPlacement(word: word, positions: positions))
                            placed = true
                            break rowLoop
                        }
                    }
                }
            }

            // If still not placed, the word genuinely doesn't fit — skip it
            // rather than crash, so a couple of long/odd words never break generation.
        }

        let letters = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
        for i in 0..<grid.count {
            if grid[i] == " " {
                grid[i] = String(letters.randomElement()!)
            }
        }

        return PuzzleData(
            grid: grid,
            placements: placements,
            gridSize: size
        )
    }

    // MARK: - Placement with best-overlap search + random fallback
    private func placeWord(
        word: String,
        dir: (row: Int, col: Int),
        size: Int,
        grid: inout [String],
        placements: inout [WordPlacement]
    ) -> Bool {

        var bestRow = -1
        var bestCol = -1
        var bestOverlap = -1

        for row in 0..<size {
            for col in 0..<size {
                guard canPlaceWord(word: word, row: row, col: col, dir: dir, size: size, grid: grid) else { continue }

                var overlap = 0
                for (i, ch) in word.enumerated() {
                    let r = row + i * dir.row
                    let c = col + i * dir.col
                    if grid[r * size + c] == String(ch) {
                        overlap += 1
                    }
                }

                if overlap > bestOverlap {
                    bestOverlap = overlap
                    bestRow = row
                    bestCol = col
                }
            }
        }

        if bestRow != -1 {
            let positions = writeWord(word: word, row: bestRow, col: bestCol, dir: dir, size: size, grid: &grid)
            placements.append(WordPlacement(word: word, positions: positions))
            return true
        }

        // Random fallback within valid bounds for this direction
        let rowMin = dir.row < 0 ? word.count - 1 : 0
        let rowMax = dir.row > 0 ? size - word.count : size - 1
        let colMin = dir.col < 0 ? word.count - 1 : 0
        let colMax = dir.col > 0 ? size - word.count : size - 1

        guard rowMin <= rowMax, colMin <= colMax else { return false }

        for _ in 0..<300 {
            let row = Int.random(in: rowMin...rowMax)
            let col = Int.random(in: colMin...colMax)

            if canPlaceWord(word: word, row: row, col: col, dir: dir, size: size, grid: grid) {
                let positions = writeWord(word: word, row: row, col: col, dir: dir, size: size, grid: &grid)
                placements.append(WordPlacement(word: word, positions: positions))
                return true
            }
        }

        return false
    }

    // MARK: - Validation
    private func canPlaceWord(
        word: String,
        row: Int,
        col: Int,
        dir: (row: Int, col: Int),
        size: Int,
        grid: [String]
    ) -> Bool {

        for (i, ch) in word.enumerated() {
            let r = row + i * dir.row
            let c = col + i * dir.col

            guard r >= 0, r < size, c >= 0, c < size else { return false }

            let existing = grid[r * size + c]
            if existing != " " && existing != String(ch) {
                return false
            }
        }
        return true
    }

    // MARK: - Write word into grid
    private func writeWord(
        word: String,
        row: Int,
        col: Int,
        dir: (row: Int, col: Int),
        size: Int,
        grid: inout [String]
    ) -> [Int] {

        var positions: [Int] = []

        for (i, ch) in word.enumerated() {
            let r = row + i * dir.row
            let c = col + i * dir.col
            let index = r * size + c
            grid[index] = String(ch)
            positions.append(index)
        }

        return positions
    }
}

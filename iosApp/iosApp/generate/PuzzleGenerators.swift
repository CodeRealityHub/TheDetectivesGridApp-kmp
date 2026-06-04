//
//  PuzzleGenerators.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

final class PuzzleGenerators {

    private let gridSize = 9

    private let allDirections: [(Int, Int)] = [
        (0, 1),    // Horizontal forward
        (1, 0),    // Vertical forward
        (1, 1),    // Diagonal down-right
        (0, -1),   // Horizontal backward
        (-1, 0),   // Vertical backward
        (-1, -1),  // Diagonal up-left
        (1, -1),   // Diagonal down-left
        (-1, 1)    // Diagonal up-right
    ]

    func generate(words: [String]) -> PuzzleData {

        var grid = Array(
            repeating: Array(repeating: Character(" "), count: gridSize),
            count: gridSize
        )

        var placements: [WordPlacement] = []

        let sortedWords = words
            .map { $0.uppercased() }
            .sorted { $0.count > $1.count }

        for (index, word) in sortedWords.enumerated() {

            let direction: (Int, Int)

            switch index % 3 {
            case 0:
                direction = (0, 1)
            case 1:
                direction = (1, 0)
            default:
                direction = (1, 1)
            }

            let placed = placeWord(
                grid: &grid,
                word: word,
                rowStep: direction.0,
                colStep: direction.1,
                placements: &placements
            )

            if !placed {

                var success = false

                for dir in allDirections.shuffled() {

                    if placeWord(
                        grid: &grid,
                        word: word,
                        rowStep: dir.0,
                        colStep: dir.1,
                        placements: &placements
                    ) {
                        success = true
                        break
                    }
                }

                if !success {
                    forcePlaceWord(
                        grid: &grid,
                        word: word,
                        placements: &placements
                    )
                }
            }
        }

        fillEmptyCells(grid: &grid)

        return PuzzleData(
            grid: grid.flatMap { row in
                row.map { String($0) }
            },
            placements: placements
        )
    }

    // MARK: Force Placement

    private func forcePlaceWord(
        grid: inout [[Character]],
        word: String,
        placements: inout [WordPlacement]
    ) {

        let directions = [
            (0, 1),
            (1, 0)
        ]

        for (rowStep, colStep) in directions {

            for row in 0..<gridSize {

                for col in 0..<gridSize {

                    if canPlaceWord(
                        grid: grid,
                        word: word,
                        startRow: row,
                        startCol: col,
                        rowStep: rowStep,
                        colStep: colStep
                    ) {

                        let positions = writeWord(
                            grid: &grid,
                            word: word,
                            startRow: row,
                            startCol: col,
                            rowStep: rowStep,
                            colStep: colStep
                        )

                        placements.append(
                            WordPlacement(
                                word: word,
                                positions: positions
                            )
                        )

                        return
                    }
                }
            }
        }

        print("CRITICAL: Could not place \(word)")
    }

    // MARK: Place Word

    private func placeWord(
        grid: inout [[Character]],
        word: String,
        rowStep: Int,
        colStep: Int,
        placements: inout [WordPlacement]
    ) -> Bool {

        let chars = Array(word)

        // Overlap Pass

        for row in 0..<gridSize {

            for col in 0..<gridSize {

                guard canPlaceWord(
                    grid: grid,
                    word: word,
                    startRow: row,
                    startCol: col,
                    rowStep: rowStep,
                    colStep: colStep
                ) else {
                    continue
                }

                var overlapCount = 0

                for i in chars.indices {

                    let r = row + i * rowStep
                    let c = col + i * colStep

                    if grid[r][c] != " " &&
                        grid[r][c] == chars[i] {
                        overlapCount += 1
                    }
                }

                if overlapCount > 0 {

                    let positions = writeWord(
                        grid: &grid,
                        word: word,
                        startRow: row,
                        startCol: col,
                        rowStep: rowStep,
                        colStep: colStep
                    )

                    placements.append(
                        WordPlacement(
                            word: word,
                            positions: positions
                        )
                    )

                    return true
                }
            }
        }

        // Random Pass

        let rowMin = rowStep < 0 ? word.count - 1 : 0
        let rowMax = rowStep > 0 ? gridSize - word.count : gridSize - 1

        let colMin = colStep < 0 ? word.count - 1 : 0
        let colMax = colStep > 0 ? gridSize - word.count : gridSize - 1

        guard rowMin <= rowMax,
              colMin <= colMax else {
            return false
        }

        for _ in 0..<500 {

            let row = Int.random(in: rowMin...rowMax)
            let col = Int.random(in: colMin...colMax)

            if canPlaceWord(
                grid: grid,
                word: word,
                startRow: row,
                startCol: col,
                rowStep: rowStep,
                colStep: colStep
            ) {

                let positions = writeWord(
                    grid: &grid,
                    word: word,
                    startRow: row,
                    startCol: col,
                    rowStep: rowStep,
                    colStep: colStep
                )

                placements.append(
                    WordPlacement(
                        word: word,
                        positions: positions
                    )
                )

                return true
            }
        }

        return false
    }

    // MARK: Can Place

    private func canPlaceWord(
        grid: [[Character]],
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ) -> Bool {

        let chars = Array(word)

        for i in chars.indices {

            let row = startRow + i * rowStep
            let col = startCol + i * colStep

            if row < 0 || row >= gridSize ||
                col < 0 || col >= gridSize {
                return false
            }

            let existing = grid[row][col]

            if existing != " " &&
                existing != chars[i] {
                return false
            }
        }

        return true
    }

    // MARK: Write Word

    private func writeWord(
        grid: inout [[Character]],
        word: String,
        startRow: Int,
        startCol: Int,
        rowStep: Int,
        colStep: Int
    ) -> [Int] {

        let chars = Array(word)

        var positions: [Int] = []

        for i in chars.indices {

            let row = startRow + i * rowStep
            let col = startCol + i * colStep

            grid[row][col] = chars[i]

            positions.append(
                row * gridSize + col
            )
        }

        return positions
    }

    // MARK: Fill Empty Cells

    private func fillEmptyCells(
        grid: inout [[Character]]
    ) {

        for row in 0..<gridSize {

            for col in 0..<gridSize {

                if grid[row][col] == " " {

                    let letter = Character(
                        UnicodeScalar(
                            Int.random(in: 65...90)
                        )!
                    )

                    grid[row][col] = letter
                }
            }
        }
    }
}

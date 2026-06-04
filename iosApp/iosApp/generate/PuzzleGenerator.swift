//
//  PuzzleGenerator.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

final class PuzzleGenerator {

    private let size = 9

    func generate(words: [String]) -> PuzzleData? {

        var grid = Array(
            repeating: "",
            count: size * size
        )

        var placements: [WordPlacement] = []

        for (row, word) in words.prefix(size).enumerated() {

            let upperWord = word.uppercased()

            guard upperWord.count <= size else {
                continue
            }

            var positions: [Int] = []

            for (column, letter) in upperWord.enumerated() {

                let index = row * size + column

                grid[index] = String(letter)

                positions.append(index)
            }

            placements.append(
                WordPlacement(
                    word: upperWord,
                    positions: positions
                )
            )
        }

        let letters = Array("ABCDEFGHIJKLMNOPQRSTUVWXYZ")

        for i in 0..<grid.count {

            if grid[i].isEmpty {

                grid[i] = String(
                    letters.randomElement()!
                )
            }
        }

        return PuzzleData(
            grid: grid,
            placements: placements
        )
    }
}

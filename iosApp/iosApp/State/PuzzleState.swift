//
//  PuzzleState.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

final class PuzzleState: ObservableObject {

    @Published var foundWords: [String: [Int]] = [:]

    func reset() {
        foundWords.removeAll()
    }
}



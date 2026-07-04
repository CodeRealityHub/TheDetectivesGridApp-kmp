//
//  WordPlacement.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

struct WordPlacement: Identifiable {
    let id = UUID()
    let word: String
    let positions: [Int]
}

//
//  CaseStorage.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

import Foundation

final class CaseStorage {

    private let key = "detective_case"

    func saveCase(caseData: CaseData) {

        guard let data = try? JSONEncoder().encode(caseData) else {
            return
        }

        UserDefaults.standard.set(data, forKey: key)
    }

    func getCase() -> CaseData? {

        guard let data = UserDefaults.standard.data(forKey: key) else {
            return nil
        }

        return try? JSONDecoder().decode(
            CaseData.self,
            from: data
        )
    }

    func clearCase() {
        UserDefaults.standard.removeObject(forKey: key)
    }
}

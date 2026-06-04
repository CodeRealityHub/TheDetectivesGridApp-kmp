package com.example.thedetectivesgrid.models

object CaseStorage {

    private val caseData = CaseData(
        culprit = "ROBERT",
        weapon  = "POISON",
        scene   = "MANSION",
        motive  = "REVENGE"
    )
    private var currentCase: CaseData? = null
    fun saveCase(caseData: CaseData) {
        currentCase = caseData
    }

    fun getCase(): CaseData? {
        return currentCase
    }
}
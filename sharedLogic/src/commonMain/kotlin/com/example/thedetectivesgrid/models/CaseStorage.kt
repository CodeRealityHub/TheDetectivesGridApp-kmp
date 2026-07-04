package com.example.thedetectivesgrid.models

object CaseStorage {

    private val defaultCase = CaseData(
        culprit = "ROBERT",
        weapon  = "POISON",
        scene   = "MANSION",
        motive  = "REVENGE"
    )

    private var currentCase: CaseData? = defaultCase // default pre-filled

    fun saveCase(caseData: CaseData) {
        currentCase = caseData
    }

    fun getCase(): CaseData? = currentCase

    fun resetToDefault() {
        currentCase = defaultCase
    }

    /** Read-only access to the built-in default case, e.g. for preview UI. */
    fun getDefaultCase(): CaseData = defaultCase
}
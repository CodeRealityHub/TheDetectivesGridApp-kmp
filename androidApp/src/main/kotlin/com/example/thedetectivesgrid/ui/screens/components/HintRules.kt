package com.example.thedetectivesgrid.ui.screens.components

object HintRules {

    // Setup phase in PuzzleScreen (before the grid is generated)
    val puzzleScreen = listOf(
        "Enter your own answers for all four categories: Culprit, Weapon, Scene, and Motive to create a mystery for the detective to solve.",
        "Words must contain 3–12 characters.",
        "Four categories: Culprit, Weapon, Scene and Motive.",
        "At least 4 words per category.",
        "When the toggle button is ON: You don't need to enter words for the four categories: Culprit, Weapon, Scene, and Motive.",
        "When the toggle button is OFF: Enter your own words for all four categories: Culprit, Weapon, Scene, and Motive.",
        "Click GENERATE PUZZLE when ready."
    )

    // Playing phase (after the grid is generated), used by both screens
    val gridScreen = listOf(
        "Find the words from the WORDS TO FIND list.",
        "Tap individual characters, one at a time, to form the word you are looking for.",
        "A word is valid only if its letters are continuous, not alternate.",
        "Words can run upward, downward, forward, backward, horizontally, vertically or diagonally.",
        "Words can overlap each other.",
        "Yellow letters are selected. Green letters are a correct word.",
        "Once a word is found, it strikes through in the list.",
        "Solve the case after finding all words."
    )

    // Setup phase in PuzzleCreatorScreen
    val creatorScreen = listOf(
        "Enter your own answers for all four categories: Culprit, Weapon, Scene, and Motive to create a mystery for the detective to solve.",
        "Each word must have 3–12 letters.",
        "Enter at least 13 words, separated by commas, to generate the grid.",
        "Click GENERATE PUZZLE & START PLAY when ready.",
        "The case can only be solved after you find every word in the grid."
    )
}

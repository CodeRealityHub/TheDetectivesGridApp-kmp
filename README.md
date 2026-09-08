🕵️ Detective Word Puzzle Game

Detective Word Puzzle Game is a native mobile word-search mystery game developed separately for Android using Kotlin and Jetpack Compose and iOS using Swift and SwiftUI.

Players investigate a mystery by finding hidden words within a puzzle grid. Words can appear horizontally, vertically, and diagonally in both forward and backward directions. Each discovered word provides a clue that helps players identify the culprit, weapon, method, and location and ultimately solve the case.

The project combines traditional word-search gameplay with a detective-style investigation system while demonstrating practical game logic, grid-based algorithms, state management, and native mobile UI development.

🎯 Why This Project?

Traditional word-search games focus primarily on finding hidden words. This project adds an additional mystery-solving layer, where every discovered word contributes to solving a case.

Players must carefully search the grid, collect relevant clues, and use them to determine what happened.

The project demonstrates how native mobile applications can combine grid generation, directional word-search algorithms, game state management, clue tracking, and interactive gameplay into a complete puzzle experience.

✨ Features

* 🕵️ Detective-themed mystery gameplay
* 🔎 Hidden-word puzzle grid
* ↔️ Horizontal word detection
* ↕️ Vertical word detection
* ↗️ Diagonal word detection
* 🔄 Forward and backward word directions
* 🧩 Multiple hidden clues
* 👆 Interactive word selection
* 📝 Clue tracking
* 🧑‍⚖️ Identify the culprit
* 🔪 Identify the weapon
* 🧪 Identify the method
* 📍 Identify the location
* 🧠 Case-solving gameplay
* 🏆 Case completion system
* 🔄 Game state management
* 📱 Native Android and iOS UI
* ⚡ Lightweight gameplay experience

🔄 Game Workflow

Start Case → Read Case Information → Search Puzzle Grid → Find Hidden Words → Collect Clues → Identify Suspect → Identify Weapon → Identify Method → Identify Location → Solve The Case

🧩 Main Game Modules

🕵️ Case Investigation

Each mystery begins with a case that players must investigate.

The player searches the puzzle grid for hidden words representing important clues related to the case.

🔎 Word Search Grid

The main gameplay area contains a grid of letters where clues are hidden in multiple directions.

Word Search Directions: → Horizontal • ← Horizontal Reverse • ↓ Vertical • ↑ Vertical Reverse • ↘ Diagonal • ↖ Diagonal Reverse • ↙ Diagonal • ↗ Diagonal Reverse

This makes the puzzle more challenging than a basic left-to-right word search.

🧠 Word Detection Logic

The game checks potential word paths across the grid using directional movement.

Select Starting Cell → Check Direction → Traverse Grid → Compare Letters → Word Found? → Yes: Save Clue • No: Try Next Direction

The algorithm evaluates multiple directions while ensuring that the search remains within the puzzle boundaries.

📝 Clue System

Successfully discovered words become clues that help players understand and solve the case.

Clues can be categorized into:

* 🧑 Culprit
* 🔪 Weapon
* 🧪 Method
* 📍 Location

🧑‍⚖️ Case Resolution

After collecting the required clues, players can identify the different elements of the mystery.

Case Resolution: Culprit + Weapon + Method + Location → Complete Case

The case is solved when the required clues are correctly identified.

🏗️ Game Architecture

The project uses separate native implementations for Android and iOS while maintaining the same core gameplay concept.

Game Architecture: Android (Kotlin + Jetpack Compose) + iOS (Swift + SwiftUI) → Game State → Puzzle Logic → Word Search Algorithm → Clue Management

The architecture separates the user interface, game state, puzzle logic, and clue-management functionality to keep the project organized and maintainable.

🛠️ Tech Stack

Kotlin • Jetpack Compose • Android SDK • Swift • SwiftUI • iOS SDK • Game Logic • Grid Algorithms • State Management

📁 Project Structure

DetectiveWordPuzzle/
 → Android/ • Kotlin • Jetpack Compose • AndroidManifest.xml • build.gradle.kts
 → iOS/ • Swift • SwiftUI • Assets.xcassets • Info.plist
 → README.md

🚀 Getting Started

🤖 Android — Kotlin

Prerequisites

* Android Studio
* JDK
* Android SDK
* Android Emulator or physical Android device
* Gradle

Open the Android project in Android Studio and allow Gradle to sync.

Run the application directly from Android Studio using the Run ▶ button.

Alternatively, from the Android project directory:

./gradlew installDebug

To build a debug APK:

./gradlew assembleDebug

🍎 iOS — Swift / SwiftUI

Prerequisites

* macOS
* Xcode
* iOS Simulator or physical iPhone
* Apple Developer account for physical-device deployment

Open the iOS project in Xcode:

open iOS/<ProjectName>.xcodeproj

Select an iPhone Simulator or connected physical device and click Run ▶.

If the project uses CocoaPods:

open iOS/<ProjectName>.xcworkspace

The project can also be built from the command line:

xcodebuild -project iOS/<ProjectName>.xcodeproj -scheme <SchemeName> -sdk iphonesimulator build

💡 Real-World Gameplay Example

A player receives a mystery involving a crime at a specific location.

They begin searching the puzzle grid and discover words such as:

SUSPECT • KNIFE • POISON • MANSION

Each word provides an important piece of information.

The player uses these clues to determine:

Culprit → Suspect • Weapon → Knife • Method → Poison • Location → Mansion

Once the required clues are correctly identified, the player successfully solves the case.

🎯 Project Goals

The main goals of this project are:

* Build an engaging detective-themed puzzle game.
* Implement a functional word-search game engine.
* Support horizontal, vertical, and diagonal word placement.
* Support forward and backward word directions.
* Implement grid-based word detection algorithms.
* Create a clue-based investigation system.
* Connect discovered words with case-solving logic.
* Manage interactive game state.
* Develop native Android gameplay using Kotlin and Jetpack Compose.
* Develop native iOS gameplay using Swift and SwiftUI.
* Maintain consistent gameplay across Android and iOS.
* Build a unique and engaging portfolio project.

📌 Portfolio Highlights

This project demonstrates practical experience with:

Kotlin • Jetpack Compose • Android Development • Swift • SwiftUI • iOS Development • Game Logic • Grid-Based Algorithms • Word Search Algorithms • Directional Searching • State Management • Interactive Gameplay • Clue Management • Puzzle Generation • Case-Solving Logic • Cross-Platform Product Development

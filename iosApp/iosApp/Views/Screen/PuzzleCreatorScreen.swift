//
//  PuzzleCreatorScreen.swift
//  iosApp
//
//  Created by Dungeon_master on 04/06/26.
//

enum ScreenState {
    case input
    case playing
}

import SwiftUI

struct PuzzleCreatorScreen: View {

    var onBack: () -> Void = {}
    var onGoToPuzzleScreen: () -> Void = {}
    var onGoToArchiveScreen: () -> Void = {}

    @State private var state: ScreenState = .input
    
    let generator = PuzzleGenerators()

    @State private var culprit = ""
    @State private var weapon = ""
    @State private var scene = ""
    @State private var motive = ""

    @State private var wordInput = ""

    @State private var culpritGuess = ""
    @State private var weaponGuess = ""
    @State private var sceneGuess = ""
    @State private var motiveGuess = ""

    @State private var caseSolved = false

    @State private var generatedGrid: [String] = []
    @State private var placements: [WordPlacement] = []

    @State private var solvedWords: Set<String> = []
    @State private var selectedCells: Set<Int> = []
    @State private var foundCells: Set<Int> = []

    private var wordList: [String] {
        wordInput
            .split(separator: ",")
            .map {
                $0.trimmingCharacters(in: .whitespaces)
                    .uppercased()
            }
            .filter { !$0.isEmpty }
    }

    var body: some View {

        ScrollView {

            VStack(spacing: 20) {

                topBar

                if state == .input {
                    inputView
                }

                if state == .playing {
                    puzzleView
                }
            }
            .padding()
        }
        .background(Color(hex: 0xC2A574))
    }
}

extension PuzzleCreatorScreen {

    private var topBar: some View {

        HStack {
            Text("Puzzle Creator Mode")
                .font(.title2.bold())

            Spacer()

            HStack {
                Button(action: onBack) {
                    Image(systemName: "square.grid.3x3")
                }
            }
        }
    }
}

extension PuzzleCreatorScreen {

    private var inputView: some View {

        VStack(spacing: 16) {

            TextField("Culprit", text: $culprit)
                .textFieldStyle(.roundedBorder)

            TextField("Weapon", text: $weapon)
                .textFieldStyle(.roundedBorder)

            TextField("Scene", text: $scene)
                .textFieldStyle(.roundedBorder)

            TextField("Motive", text: $motive)
                .textFieldStyle(.roundedBorder)

            TextField(
                "Enter Words (comma separated)",
                text: $wordInput,
                axis: .vertical
            )
            .textFieldStyle(.roundedBorder)

            Button("Generate Puzzle & Start Play") {
             
                let puzzle =
                    generator.generate(words: wordList)

                generatedGrid = puzzle.grid
                placements = puzzle.placements

                state = .playing
            }
            .buttonStyle(.borderedProminent)
        }
        .padding()
        .background(.white.opacity(0.8))
        .cornerRadius(20)
    }
}

extension PuzzleCreatorScreen {

    private var puzzleView: some View {

        VStack(spacing: 20) {

            LazyVGrid(
                columns: Array(
                    repeating: GridItem(.flexible()),
                    count: 9
                )
            ) {

                ForEach(
                    Array(generatedGrid.enumerated()),
                    id: \.offset
                ) { index, letter in

                    PuzzleCell(
                        letter: letter,
                        selected:
                            selectedCells.contains(index) ||
                            foundCells.contains(index)
                    )
                    .onTapGesture {

                        if selectedCells.contains(index) {
                            selectedCells.remove(index)
                        } else {
                            selectedCells.insert(index)
                        }
                    }
                }
            }

            Button("Check Word") {

                let selected =
                    selectedCells.sorted()

                if let match = placements.first(
                    where: {
                        $0.positions.sorted() == selected
                    }
                ) {

                    solvedWords.insert(match.word)

                    match.positions.forEach {
                        foundCells.insert($0)
                    }
                }

                selectedCells.removeAll()
            }

            wordsCard

            answerFields

            Button("Solve Case") {

                caseSolved =
                    culpritGuess == culprit &&
                    weaponGuess == weapon &&
                    sceneGuess == scene &&
                    motiveGuess == motive
            }

            Text(
                caseSolved
                ? "CASE SOLVED ✅"
                : "Case Not Solved Yet ❌"
            )
            .foregroundColor(
                caseSolved ? .green : .red
            )
            .font(.headline)
        }
    }
}

extension PuzzleCreatorScreen {

    private var wordsCard: some View {

        VStack(alignment: .leading, spacing: 8) {

            Text("Words To Find")
                .font(.headline)

            ForEach(wordList, id: \.self) { word in

                Text(word)
                    .strikethrough(
                        solvedWords.contains(word)
                    )
            }
        }
        .padding()
        .background(.white.opacity(0.8))
        .cornerRadius(16)
    }
}

extension PuzzleCreatorScreen {

    private var answerFields: some View {

        VStack(spacing: 12) {

            TextField(
                "Enter Culprit",
                text: $culpritGuess
            )
            .textFieldStyle(.roundedBorder)

            TextField(
                "Enter Weapon",
                text: $weaponGuess
            )
            .textFieldStyle(.roundedBorder)

            TextField(
                "Enter Scene",
                text: $sceneGuess
            )
            .textFieldStyle(.roundedBorder)

            TextField(
                "Enter Motive",
                text: $motiveGuess
            )
            .textFieldStyle(.roundedBorder)
        }
    }
}

struct PuzzleCell: View {

    let letter: String
    let selected: Bool

    var body: some View {

        ZStack {

            RoundedRectangle(cornerRadius: 6)
                .fill(
                    selected
                    ? Color.green.opacity(0.4)
                    : Color.yellow.opacity(0.3)
                )

            Text(letter)
                .fontWeight(.bold)
        }
        .aspectRatio(1, contentMode: .fit)
    }
}

extension Color {

    init(hex: UInt32) {

        self.init(
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255
        )
    }
}


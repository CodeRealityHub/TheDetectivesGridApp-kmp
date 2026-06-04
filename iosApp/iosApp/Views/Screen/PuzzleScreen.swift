import SwiftUI

// MARK: - Colour Tokens
extension Color {

    init(hexValue: UInt32) {

        self.init(

            red: Double((hexValue >> 16) & 0xFF) / 255.0,

            green: Double((hexValue >> 8) & 0xFF) / 255.0,

            blue: Double(hexValue & 0xFF) / 255.0

        )

    }

}

private extension Color {
    static let bgSand          = Color(hexValue: 0xC2A574)
    static let cardCream       = Color(hexValue: 0xF4E6D0)
    static let cardCream2      = Color(hexValue: 0xF8EEDC)
    static let borderBeige     = Color(hexValue: 0xD9C2A0)
    static let borderBeige2    = Color(hexValue: 0xD7BE9A)
    static let shadowBrown     = Color(hexValue: 0x8D6E63)
    static let shadowBrown2    = Color(hexValue: 0xB08B62)
    static let titleBrown      = Color(hexValue: 0x3E2723)
    static let subBrown        = Color(hexValue: 0x5D4037)
    static let textBrown       = Color(hexValue: 0x4E342E)
    static let bulletBrown     = Color(hexValue: 0x6D4C41)
    static let greenSolved     = Color(hexValue: 0x2E7D32)
    static let redUnsolved     = Color(hexValue: 0xC62828)
    static let highlightYellow = Color(hexValue: 0xFFE082)
    static let highlightGreen  = Color(hexValue: 0xA5D6A7)
    static let cellBase        = Color(hexValue: 0xEDD9BB)
}

// MARK: - Constants
private let gridSize = 9
private let minWords = 13
private let juraFont = "Jura-Bold"


// MARK: - PuzzleScreen
struct PuzzleScreen: View {

    var onArchiveClick:       () -> Void = {}
    var onPuzzleCreatorClick: () -> Void = {}
    var caseNumber: String = "1"
    @State private var isPlaying       = false
    @State private var wordInputText   = ""
    @State private var wordList:        [String] = []
    @State private var wordInputError  = ""
    @State private var puzzleData:      PuzzleData? = nil
    @State private var selectedCells:   Set<Int> = []
    @State private var foundWords:      [String: [Int]] = [:]
    @State private var culpritInput    = ""
    @State private var weaponInput     = ""
    @State private var sceneInput      = ""
    @State private var motiveInput     = ""
    @State private var showAnswerForm  = false
    @State private var caseResult:      Bool? = nil
    @State private var generateError   = ""   // ← new: surface Kotlin crashes to UI

    @FocusState private var focusedField: AnswerField?
    enum AnswerField { case word, culprit, weapon, scene, motive }

    var body: some View {
        ZStack {
            Color.bgSand.ignoresSafeArea()
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    topBar
                    if !isPlaying {
                        wordInputPhase
                    }
                    if isPlaying, let puzzle = puzzleData {
                        playingPhase(puzzle: puzzle)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.top, 16)
                .padding(.bottom, 32)
            }
        }
        .navigationBarHidden(true)
    }

    // MARK: - Top Bar
    private var topBar: some View {
        HStack(alignment: .top) {

            VStack(alignment: .leading, spacing: 4) {

                Text("The Detective's Grid")
                    .font(.custom(juraFont, size: 26))
                    .fontWeight(.heavy)
                    .foregroundColor(.titleBrown)

                Text("CASE \(caseNumber)")
                    .font(.custom(juraFont, size: 16))
                    .fontWeight(.heavy)
                    .foregroundColor(.redUnsolved)

                Text(isPlaying ? "Case File" : "Setup Case \(caseNumber)")
                    .font(.custom(juraFont, size: 14))
                    .italic()
                    .fontWeight(.bold)
                    .foregroundColor(.subBrown)
            }

            Spacer()

            HStack(spacing: 8) {

                // Archive Button
                Button(action: onArchiveClick) {
                    Image(systemName: "folder.fill")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.titleBrown)
                        .frame(width: 36, height: 36)
                        .background(Color.cardCream)
                        .cornerRadius(10)
                }

                // Creator Button
                Button(action: onPuzzleCreatorClick) {
                    Image(systemName: "pencil")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundColor(.titleBrown)
                        .frame(width: 36, height: 36)
                        .background(Color.cardCream)
                        .cornerRadius(10)
                }
            }
        }
    }
    // MARK: - Word Input Phase
    private var wordInputPhase: some View {
        NotebookCard {
            Text("Add Words to Find")
                .font(.custom(juraFont, size: 20))
                .fontWeight(.heavy)
                .foregroundColor(.titleBrown)

            Text("Minimum \(minWords) words required")
                .font(.custom(juraFont, size: 12))
                .foregroundColor(.bulletBrown)
                .padding(.top, 2)

            Divider()
                .background(Color.borderBeige2)
                .padding(.vertical, 10)

            // Input row
            HStack(spacing: 8) {
                TextField("", text: $wordInputText, prompt:
                    Text("Enter a word…")
                        .foregroundColor(Color.bulletBrown.opacity(0.6))
                )
                .font(.custom(juraFont, size: 15))
                .fontWeight(.bold)
                .foregroundColor(.titleBrown)
                .focused($focusedField, equals: .word)
                .autocorrectionDisabled()
                .textInputAutocapitalization(.characters)
                .onChange(of: wordInputText) { v in
                    wordInputText = v.uppercased().filter { $0.isLetter }
                    wordInputError = ""
                }
                .onSubmit { submitWord() }
                .padding(.horizontal, 12)
                .padding(.vertical, 11)
                .background(Color.white.opacity(0.5))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(
                            focusedField == .word ? Color.titleBrown : Color.borderBeige,
                            lineWidth: 1
                        )
                )
                .cornerRadius(12)

                Button(action: submitWord) {
                    Image(systemName: "plus")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 44, height: 44)
                        .background(Color.titleBrown)
                        .cornerRadius(12)
                }
            }

            // Validation error
            if !wordInputError.isEmpty {
                Text(wordInputError)
                    .font(.custom(juraFont, size: 12))
                    .foregroundColor(.redUnsolved)
                    .padding(.top, 4)
            }

            // Generate error (KMP crash message)
            if !generateError.isEmpty {
                HStack(spacing: 6) {
                    Image(systemName: "exclamationmark.triangle.fill")
                        .font(.system(size: 11))
                        .foregroundColor(.redUnsolved)
                    Text(generateError)
                        .font(.custom(juraFont, size: 12))
                        .foregroundColor(.redUnsolved)
                        .fixedSize(horizontal: false, vertical: true)
                }
                .padding(.top, 6)
            }

            // Chips
            if !wordList.isEmpty {
                WordChipsGrid(words: wordList, onRemove: { w in
                    wordList.removeAll { $0 == w }
                })
                .padding(.top, 12)
            }

            // Progress row
            HStack {
                Text("\(wordList.count) / \(minWords)+ words")
                    .font(.custom(juraFont, size: 13))
                    .fontWeight(.bold)
                    .foregroundColor(wordList.count >= minWords ? .greenSolved : .bulletBrown)
                Spacer()
                if wordList.count >= minWords {
                    Text("✓ Ready!")
                        .font(.custom(juraFont, size: 13))
                        .fontWeight(.heavy)
                        .foregroundColor(.greenSolved)
                }
            }
            .padding(.top, 12)

            // Generate button
            Button(action: generatePuzzle) {
                HStack(spacing: 8) {
                    Image(systemName: "magnifyingglass")
                        .foregroundColor(.white)
                    Text("Generate Puzzle")
                        .font(.custom(juraFont, size: 15))
                        .fontWeight(.heavy)
                        .foregroundColor(.white)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(
                    wordList.count >= minWords
                        ? Color.titleBrown
                        : Color.titleBrown.opacity(0.35)
                )
                .cornerRadius(14)
            }
            .disabled(wordList.count < minWords)
            .padding(.top, 14)
        }
    }

    // MARK: - Playing Phase
    @ViewBuilder
    private func playingPhase(puzzle: PuzzleData) -> some View {
        let allWordsFound = foundWords.count == wordList.count

        NotebookCard {
            HStack {
                Text("Investigation Notes")
                    .font(.custom(juraFont, size: 20))
                    .fontWeight(.heavy)
                    .foregroundColor(.titleBrown)
                Spacer()
                Button(action: resetToInput) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrow.counterclockwise")
                            .font(.system(size: 12))
                            .foregroundColor(.bulletBrown)
                        Text("Reset")
                            .font(.custom(juraFont, size: 12))
                            .foregroundColor(.bulletBrown)
                    }
                }
            }

            Divider()
                .background(Color.borderBeige2)
                .padding(.vertical, 10)

            PuzzleGridView(
                puzzle: puzzle,
                foundWords: foundWords,
                selectedCells: selectedCells,
                onSelectionChanged: { selectedCells = $0 },
                onWordFound: { word, positions in
                    foundWords[word] = positions
                    selectedCells = []
                }
            )

            NotepadCard {
                Text("Words To Find")
                    .font(.custom(juraFont, size: 18))
                    .fontWeight(.heavy)
                    .italic()
                    .foregroundColor(.titleBrown)

                Text("\(foundWords.count) / \(wordList.count) found")
                    .font(.custom(juraFont, size: 12))
                    .fontWeight(.bold)
                    .foregroundColor(foundWords.count == wordList.count ? .greenSolved : .bulletBrown)
                    .padding(.top, 2)

                Divider()
                    .background(Color.borderBeige2)
                    .padding(.vertical, 8)

                VStack(spacing: 8) {
                    ForEach(wordList, id: \.self) { word in
                        let isFound    = foundWords[word] != nil
                        // Only grey out unfound words AFTER the verdict has been submitted
                        let isDisabled = caseResult != nil && !isFound

                        HStack(spacing: 10) {
                            Circle()
                                .fill(
                                    isDisabled ? Color.bulletBrown.opacity(0.3)
                                    : isFound  ? Color.greenSolved
                                    :            Color.bulletBrown
                                )
                                .frame(width: 7, height: 7)

                            Text(word)
                                .font(.custom(juraFont, size: 15))
                                .fontWeight(.heavy)
                                .foregroundColor(
                                    isDisabled ? Color.textBrown.opacity(0.35)
                                    : isFound  ? .greenSolved
                                    :            .textBrown
                                )
                                .strikethrough(isFound, color: .greenSolved)

                            if isFound {
                                Text("✓")
                                    .font(.custom(juraFont, size: 13))
                                    .foregroundColor(.greenSolved)
                            }

                            if isDisabled {
                                Text("✗")
                                    .font(.custom(juraFont, size: 13))
                                    .foregroundColor(Color.redUnsolved.opacity(0.5))
                            }

                            Spacer()
                        }
                        .opacity(isDisabled ? 0.55 : 1.0)
                    }
                }
            }
            .padding(.top, 16)
        }

        // All words found banner
        if allWordsFound {
            HStack(spacing: 12) {
                Image(systemName: "checkmark.circle.fill")
                    .resizable()
                    .frame(width: 26, height: 26)
                    .foregroundColor(.greenSolved)
                VStack(alignment: .leading, spacing: 2) {
                    Text("All \(wordList.count)/\(wordList.count) Clues Found!")
                        .font(.custom(juraFont, size: 15))
                        .fontWeight(.heavy)
                        .foregroundColor(.greenSolved)
                    Text("Now solve the case below ↓")
                        .font(.custom(juraFont, size: 12))
                        .foregroundColor(Color.greenSolved.opacity(0.8))
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(14)
            .background(Color.greenSolved.opacity(0.12))
            .overlay(RoundedRectangle(cornerRadius: 14).stroke(Color.greenSolved, lineWidth: 1))
            .cornerRadius(14)
            .transition(.opacity.combined(with: .move(edge: .bottom)))
        }

        // Answer form card
        NotebookCard {
            HStack {
                Text("Solve The Case")
                    .font(.custom(juraFont, size: 20))
                    .fontWeight(.heavy)
                    .foregroundColor(.titleBrown)
                Spacer()
                Button(action: { withAnimation { showAnswerForm.toggle() } }) {
                    Text(showAnswerForm ? "Hide" : "Fill Answers")
                        .font(.custom(juraFont, size: 12))
                        .foregroundColor(.bulletBrown)
                }
            }

            Divider()
                .background(Color.borderBeige2)
                .padding(.vertical, 8)

            if showAnswerForm {
                VStack(spacing: 10) {
                    CaseAnswerField(label: "CULPRIT", value: $culpritInput, focused: $focusedField, field: .culprit)
                    CaseAnswerField(label: "WEAPON",  value: $weaponInput,  focused: $focusedField, field: .weapon)
                    CaseAnswerField(label: "SCENE",   value: $sceneInput,   focused: $focusedField, field: .scene)
                    CaseAnswerField(label: "MOTIVE",  value: $motiveInput,  focused: $focusedField, field: .motive)

                    Button(action: submitVerdict) {
                        HStack(spacing: 8) {
                            Image(systemName: "hammer.fill").foregroundColor(.white)
                            Text("Submit My Verdict")
                                .font(.custom(juraFont, size: 15))
                                .fontWeight(.heavy)
                                .foregroundColor(.white)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.titleBrown)
                        .cornerRadius(14)
                    }
                    .padding(.top, 6)
                }
                .transition(.opacity)
            }
        }

        resultCard(allWordsFound: allWordsFound)
    }

    // MARK: - Result Card
    private func resultCard(allWordsFound: Bool) -> some View {
        let isSolved   = caseResult == true
        let showResult = caseResult != nil

        return VStack(alignment: .leading, spacing: 8) {
            Text("Investigation Result 👉")
                .font(.custom(juraFont, size: 18))
                .fontWeight(.heavy)
                .foregroundColor(.titleBrown)

            Text("Clues: \(foundWords.count) / \(wordList.count) found\(allWordsFound ? " — All clues uncovered!" : "")")
                .font(.custom(juraFont, size: 13))
                .fontWeight(.bold)
                .foregroundColor(allWordsFound ? .greenSolved : .bulletBrown)

            if showResult {
                HStack(spacing: 10) {
                    Text(isSolved ? "Case Solved Successfully" : "Not Correct. Keep Investigating.")
                        .font(.custom(juraFont, size: 14))
                        .fontWeight(.heavy)
                        .foregroundColor(isSolved ? .greenSolved : .redUnsolved)
                        .fixedSize(horizontal: false, vertical: true)

                    Spacer()

                    HStack(spacing: 5) {
                        Image(systemName: isSolved ? "face.smiling" : "face.dashed")
                            .foregroundColor(.white)
                        Text(isSolved ? "Solved" : "Not Solved")
                            .font(.custom(juraFont, size: 12))
                            .fontWeight(.bold)
                            .foregroundColor(.white)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 9)
                    .background(isSolved ? Color.greenSolved : Color.redUnsolved)
                    .cornerRadius(12)
                }
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(Color.cardCream)
        .overlay(RoundedRectangle(cornerRadius: 16).stroke(Color.borderBeige, lineWidth: 1))
        .cornerRadius(16)
    }

    // MARK: - Actions

    private func submitWord() {
        let w = wordInputText.trimmingCharacters(in: .whitespaces).uppercased()
        if w.isEmpty {
            wordInputError = "Please enter a word."
        } else if w.count < 3 {
            wordInputError = "Word must be at least 3 letters."
        } else if w.count > gridSize {
            wordInputError = "Word too long (max \(gridSize) letters)."
        } else if wordList.contains(w) {
            wordInputError = "\"\(w)\" is already in the list."
        } else {
            wordList.append(w)
            wordInputText  = ""
            wordInputError = ""
            generateError  = ""
        }
    }

    private func generatePuzzle() {
        guard wordList.count >= minWords else {
            wordInputError = "Please add at least \(minWords) words."
            return
        }

        // ── 1. Sanitise words before handing to Kotlin ──────────────────
        let safeWords = wordList
            .map { $0.uppercased().trimmingCharacters(in: .whitespaces) }
            .filter { !$0.isEmpty && $0.count >= 3 && $0.count <= gridSize }

        guard safeWords.count >= minWords else {
            generateError = "Some words were too short or too long after sanitising. Please review your list."
            return
        }

        generateError = ""

        guard let generated = PuzzleGenerator().generate(words: safeWords) else {
            generateError = "Could not generate a puzzle with the current words."
            return
        }

        puzzleData = generated

        // ── 4. Save default case if none exists ─────────────────────────
        let storage = CaseStorage()
        if storage.getCase() == nil {
            storage.saveCase(caseData: CaseData(
                culprit: "ROBERT",
                weapon:  "POISON",
                scene:   "MANSION",
                motive:  "REVENGE"
            ))
        }

        withAnimation {
            isPlaying      = true
            foundWords     = [:]
            showAnswerForm = false
            caseResult     = nil
        }
    }

    private func resetToInput() {
        isPlaying      = false
        wordList       = []
        wordInputText  = ""
        wordInputError = ""
        generateError  = ""
    }

    private func submitVerdict() {
        let storage = CaseStorage()
        guard let c = storage.getCase() else { return }
        caseResult =
            culpritInput.trimmingCharacters(in: .whitespaces) == c.culprit.uppercased() &&
            weaponInput.trimmingCharacters(in: .whitespaces)  == c.weapon.uppercased()  &&
            sceneInput.trimmingCharacters(in: .whitespaces)   == c.scene.uppercased()   &&
            motiveInput.trimmingCharacters(in: .whitespaces)  == c.motive.uppercased()
    }
}

// MARK: - PuzzleGridView
struct PuzzleGridView: View {

    let puzzle: PuzzleData
    let foundWords: [String: [Int]]
    let selectedCells: Set<Int>
    let onSelectionChanged: (Set<Int>) -> Void
    let onWordFound: (String, [Int]) -> Void

    private var foundCells: Set<Int> {
        Set(foundWords.values.flatMap { $0 })
    }

    var body: some View {
        VStack(spacing: 0) {
            let columns = Array(repeating: GridItem(.flexible(), spacing: 3), count: gridSize)

            LazyVGrid(columns: columns, spacing: 3) {
                ForEach(0..<puzzle.grid.count, id: \.self) { index in
                    let letter  = puzzle.grid[index]
                    let isFound = foundCells.contains(index)
                    let isSel   = selectedCells.contains(index)

                    let bg: Color = isFound ? .highlightGreen
                                  : isSel   ? .highlightYellow
                                  :           .cellBase

                    ZStack {
                        RoundedRectangle(cornerRadius: 6).fill(bg)
                        RoundedRectangle(cornerRadius: 6)
                            .stroke(
                                isSel ? Color.titleBrown : Color.borderBeige2,
                                lineWidth: isSel ? 1.5 : 0.5
                            )
                        Text(letter)
                            .font(.custom(juraFont, size: 13))
                            .fontWeight(isFound || isSel ? .heavy : .bold)
                            .foregroundColor(isFound ? .greenSolved : .titleBrown)
                    }
                    .frame(maxWidth: .infinity)
                    .aspectRatio(1, contentMode: .fit)
                    .onTapGesture { handleCellTap(index) }
                }
            }

            if !selectedCells.isEmpty {
                Button(action: { onSelectionChanged([]) }) {
                    HStack(spacing: 4) {
                        Image(systemName: "xmark")
                            .font(.system(size: 11))
                            .foregroundColor(.bulletBrown)
                        Text("Clear Selection (\(selectedCells.count) cells)")
                            .font(.custom(juraFont, size: 12))
                            .foregroundColor(.bulletBrown)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.top, 8)
                }
            }
        }
    }

    private func handleCellTap(_ index: Int) {
        var newSel = selectedCells
        if newSel.contains(index) { newSel.remove(index) } else { newSel.insert(index) }

        if let match = puzzle.placements.first(where: { p in
            Set(p.positions) == newSel
        }), foundWords[match.word] == nil {

            onWordFound(match.word, match.positions)

        } else {
            onSelectionChanged(newSel)
        }
       
    }
}

// MARK: - CaseAnswerField
struct CaseAnswerField: View {
    let label: String
    @Binding var value: String
    var focused: FocusState<PuzzleScreen.AnswerField?>.Binding
    let field: PuzzleScreen.AnswerField

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.custom(juraFont, size: 11))
                .fontWeight(.heavy)
                .foregroundColor(.bulletBrown)
                .kerning(1.5)

            TextField("", text: $value, prompt:
                Text("Who / What / Where…")
                    .foregroundColor(Color.bulletBrown.opacity(0.5))
            )
            .font(.custom(juraFont, size: 15))
            .fontWeight(.bold)
            .foregroundColor(.titleBrown)
            .focused(focused, equals: field)
            .autocorrectionDisabled()
            .textInputAutocapitalization(.characters)
            .onChange(of: value) { value = $0.uppercased() }
            .padding(.horizontal, 12)
            .padding(.vertical, 10)
            .background(Color.white.opacity(0.5))
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(
                        focused.wrappedValue == field ? Color.titleBrown : Color.borderBeige,
                        lineWidth: 1
                    )
            )
            .cornerRadius(12)
        }
    }
}

// MARK: - WordChipsGrid
struct WordChipsGrid: View {
    let words: [String]
    let onRemove: (String) -> Void

    var body: some View {
        let rows = stride(from: 0, to: words.count, by: 3).map {
            Array(words[$0..<min($0 + 3, words.count)])
        }
        VStack(alignment: .leading, spacing: 8) {
            ForEach(rows, id: \.self) { row in
                HStack(spacing: 8) {
                    ForEach(row, id: \.self) { word in
                        HStack(spacing: 4) {
                            Text(word)
                                .font(.custom(juraFont, size: 12))
                                .fontWeight(.bold)
                                .foregroundColor(.titleBrown)
                            Button(action: { onRemove(word) }) {
                                Image(systemName: "xmark")
                                    .resizable()
                                    .frame(width: 7, height: 7)
                                    .foregroundColor(.bulletBrown)
                            }
                        }
                        .padding(.horizontal, 9)
                        .padding(.vertical, 5)
                        .background(Color.titleBrown.opacity(0.10))
                        .overlay(
                            RoundedRectangle(cornerRadius: 20)
                                .stroke(Color.titleBrown.opacity(0.25), lineWidth: 1)
                        )
                        .cornerRadius(20)
                    }
                    Spacer()
                }
            }
        }
    }
}

// MARK: - NotebookCard
struct NotebookCard<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0, content: content)
            .padding(18)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.cardCream)
            .cornerRadius(22)
            .overlay(
                RoundedRectangle(cornerRadius: 22)
                    .stroke(Color.borderBeige, lineWidth: 1)
            )
            .shadow(color: Color.shadowBrown.opacity(0.55), radius: 0, x: 5, y: 6)
    }
}

// MARK: - NotepadCard
struct NotepadCard<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0, content: content)
            .padding(16)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color.cardCream2)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(Color.borderBeige2, lineWidth: 1)
            )
            .shadow(color: Color.shadowBrown2.opacity(0.5), radius: 0, x: 4, y: 5)
    }
}

// MARK: - Preview
#Preview {
    PuzzleScreen(caseNumber: "1")
}

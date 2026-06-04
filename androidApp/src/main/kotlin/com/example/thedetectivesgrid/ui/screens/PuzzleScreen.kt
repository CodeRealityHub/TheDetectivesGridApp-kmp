package com.example.thedetectivesgrid.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thedetectivesgrid.generator.PuzzleGenerators
import com.example.thedetectivesgrid.models.CaseData
import com.example.thedetectivesgrid.models.CaseStorage
import com.example.thedetectivesgrid.models.PuzzleData
import com.example.thedetectivesgrid.models.PuzzleState
import com.example.thedetectivesgrid.R



// ─────────────────────────────────────────────
//  Colour tokens (keep in sync with existing UI)
// ─────────────────────────────────────────────
private val BgSand       = Color(0xFFC2A574)
private val CardCream    = Color(0xFFF4E6D0)
private val CardCream2   = Color(0xFFF8EEDC)
private val BorderBeige  = Color(0xFFD9C2A0)
private val BorderBeige2 = Color(0xFFD7BE9A)
private val ShadowBrown  = Color(0xFF8D6E63)
private val ShadowBrown2 = Color(0xFFB08B62)
private val TitleBrown   = Color(0xFF3E2723)
private val SubBrown     = Color(0xFF5D4037)
private val TextBrown    = Color(0xFF4E342E)
private val BulletBrown  = Color(0xFF6D4C41)
private val GreenSolved  = Color(0xFF2E7D32)
private val RedUnsolved  = Color(0xFFC62828)
private val HighlightYellow = Color(0xFFFFE082)
private val HighlightGreen  = Color(0xFFA5D6A7)

private val MIN_WORDS = 13
private const val GRID_SIZE = 9

@Composable
fun PuzzleScreen(
    caseNumber: String,
    onArchiveClick: () -> Unit,
    onPuzzleCreatorClick: () -> Unit
) {
    val jura = FontFamily(Font(R.font.jura))

    // ── State machine ──────────────────────────────────────
    var puzzleState by remember { mutableStateOf<PuzzleState>(PuzzleState.Input) }

    // ── Word-input phase ────────────────────────────────────
    var wordInputText  by remember { mutableStateOf("") }
    var wordList       by remember { mutableStateOf(listOf<String>()) }
    var wordInputError by remember { mutableStateOf("") }

    // ── Generated puzzle ────────────────────────────────────
    var puzzleData     by remember { mutableStateOf<PuzzleData?>(null) }

    // ── Grid selection ──────────────────────────────────────
    var selectedCells  by remember { mutableStateOf(setOf<Int>()) }
    var foundWords     by remember { mutableStateOf(mapOf<String, List<Int>>()) }

    // ── Answer inputs ───────────────────────────────────────
    var culpritInput   by remember { mutableStateOf("") }
    var weaponInput    by remember { mutableStateOf("") }
    var sceneInput     by remember { mutableStateOf("") }
    var motiveInput    by remember { mutableStateOf("") }
    var showAnswerForm by remember { mutableStateOf(false) }
    var caseResult     by remember { mutableStateOf<Boolean?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BgSand
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(top = 46.dp, bottom = 24.dp)
        ) {

            // ── TOP BAR ─────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "The Detective's Grid",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = jura,
                        color = TitleBrown
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (puzzleState is PuzzleState.Input) "Setup Case $caseNumber" else "Case File",
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        color = SubBrown,
                        textAlign = TextAlign.Center,
                        fontFamily = jura
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPuzzleCreatorClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Creator",
                            tint = TitleBrown,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(onClick = onArchiveClick) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesomeMosaic,
                            contentDescription = "Archive",
                            tint = TitleBrown,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ══════════════════════════════════════════════
            //  PHASE 1 — WORD INPUT
            // ══════════════════════════════════════════════
            if (puzzleState is PuzzleState.Input) {

                NotebookCard(jura = jura) {

                    Text(
                        text = "Add Words to Find",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TitleBrown,
                        fontFamily = jura
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Minimum $MIN_WORDS words required",
                        fontSize = 13.sp,
                        color = BulletBrown,
                        fontFamily = jura
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = BorderBeige2)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Word input row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = wordInputText,
                            onValueChange = {
                                wordInputText = it.uppercase().filter { c -> c.isLetter() }
                                wordInputError = ""
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = {
                                Text("Enter a word…", fontFamily = jura, color = BulletBrown.copy(alpha = 0.6f))
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                addWord(
                                    wordInputText,
                                    wordList,
                                    onSuccess = { newList ->
                                        wordList = newList
                                        wordInputText = ""
                                        wordInputError = ""
                                    },
                                    onError = { wordInputError = it }
                                )
                            }),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = TitleBrown,
                                unfocusedBorderColor = BorderBeige,
                                focusedTextColor = TitleBrown,
                                unfocusedTextColor = TitleBrown,
                                cursorColor = TitleBrown
                            ),
                            shape = RoundedCornerShape(12.dp),
                            textStyle = LocalTextStyle.current.copy(
                                fontFamily = jura,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Button(
                            onClick = {
                                addWord(
                                    wordInputText,
                                    wordList,
                                    onSuccess = { newList ->
                                        wordList = newList
                                        wordInputText = ""
                                        wordInputError = ""
                                    },
                                    onError = { wordInputError = it }
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = TitleBrown),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                        }
                    }

                    if (wordInputError.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = wordInputError,
                            color = RedUnsolved,
                            fontSize = 12.sp,
                            fontFamily = jura
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Word chips
                    if (wordList.isNotEmpty()) {
                        WordChipsGrid(
                            words = wordList,
                            onRemove = { w -> wordList = wordList.filter { it != w } },
                            jura = jura
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Progress indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${wordList.size} / $MIN_WORDS+ words",
                            color = if (wordList.size >= MIN_WORDS) GreenSolved else BulletBrown,
                            fontWeight = FontWeight.Bold,
                            fontFamily = jura,
                            fontSize = 14.sp
                        )
                        if (wordList.size >= MIN_WORDS) {
                            Text(
                                text = "✓ Ready!",
                                color = GreenSolved,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = jura,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Generate button
                    Button(
                        onClick = {
                            if (wordList.size < MIN_WORDS) {
                                wordInputError = "Please add at least $MIN_WORDS words."
                            } else {
                                val generated = PuzzleGenerators.generate(wordList)
                                if (generated == null) {
                                    wordInputError = "Could not place all words. Try different words."
                                } else {
                                    puzzleData = generated

                                    if (CaseStorage.getCase() == null) {
                                        CaseStorage.saveCase(
                                            CaseData(
                                                culprit = "ROBERT",
                                                weapon  = "POISON",
                                                scene   = "MANSION",
                                                motive  = "REVENGE"
                                            )
                                        )
                                    }

                                    puzzleState   = PuzzleState.Playing
                                    foundWords    = emptyMap()
                                    showAnswerForm = false
                                    caseResult    = null
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = wordList.size >= MIN_WORDS,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TitleBrown,
                            disabledContainerColor = TitleBrown.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate Puzzle",
                            fontFamily = jura,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }

            // ══════════════════════════════════════════════
            //  PHASE 2 — PLAYING
            // ══════════════════════════════════════════════
            if (puzzleState is PuzzleState.Playing && puzzleData != null) {
                val puzzle = puzzleData!!

                // ── NOTEBOOK CARD (grid + word list) ────────
                NotebookCard(jura = jura) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Investigation Notes",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TitleBrown,
                            fontFamily = jura
                        )
                        TextButton(
                            onClick = {
                                puzzleState = PuzzleState.Input
                                wordList = emptyList()
                                wordInputText = ""
                                wordInputError = ""
                            }
                        ) {
                            Icon(
                                Icons.Default.Refresh,
                                contentDescription = "Reset",
                                tint = BulletBrown,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset", fontFamily = jura, color = BulletBrown, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = BorderBeige2)
                    Spacer(modifier = Modifier.height(16.dp))

                    // ── GRID ────────────────────────────────
                    PuzzleGrid(
                        puzzle        = puzzle,
                        foundWords    = foundWords,
                        selectedCells = selectedCells,
                        onSelectionChanged = { cells -> selectedCells = cells },
                        onWordFound = { word, positions ->
                            foundWords = foundWords + (word to positions)
                            selectedCells = emptySet()
                        },
                        jura = jura
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // ── WORDS TO FIND ────────────────────────
                    NotepadCard(jura = jura) {
                        Text(
                            text = "Words To Find",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontStyle = FontStyle.Italic,
                            color = TitleBrown,
                            fontFamily = jura
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${foundWords.size} / ${wordList.size} found",
                            fontSize = 13.sp,
                            color = if (foundWords.size == wordList.size) GreenSolved else BulletBrown,
                            fontFamily = jura,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderBeige2)
                        Spacer(modifier = Modifier.height(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            wordList.forEach { word ->
                                val isFound    = foundWords.containsKey(word)
                                // Grey out only after verdict submitted AND word was not found
                                val isDisabled = caseResult != null && !isFound

                                Row(
                                    modifier = Modifier.alpha(if (isDisabled) 0.45f else 1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .size(8.dp)
                                            .background(
                                                color = when {
                                                    isDisabled -> BulletBrown.copy(alpha = 0.35f)
                                                    isFound    -> GreenSolved
                                                    else       -> BulletBrown
                                                },
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = word,
                                        fontSize = 16.sp,
                                        color = when {
                                            isDisabled -> TextBrown.copy(alpha = 0.4f)
                                            isFound    -> GreenSolved
                                            else       -> TextBrown
                                        },
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = jura,
                                        textDecoration = if (isFound) TextDecoration.LineThrough else null
                                    )
                                    if (isFound) {
                                        Text(
                                            text = "✓",
                                            color = GreenSolved,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = jura,
                                            fontSize = 14.sp
                                        )
                                    }
                                    if (isDisabled) {
                                        Text(
                                            text = "✗",
                                            color = RedUnsolved.copy(alpha = 0.5f),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = jura,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── ALL WORDS FOUND BANNER ───────────────────
                val allWordsFound = foundWords.size == wordList.size
                AnimatedVisibility(
                    visible = allWordsFound,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut()
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = GreenSolved.copy(alpha = 0.15f)),
                        border = BorderStroke(1.dp, GreenSolved)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = GreenSolved,
                                modifier = Modifier.size(28.dp)
                            )
                            Column {
                                Text(
                                    text = "All ${wordList.size}/${wordList.size} Clues Found!",
                                    color = GreenSolved,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = jura,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "Now solve the case below ↓",
                                    color = GreenSolved.copy(alpha = 0.8f),
                                    fontFamily = jura,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }

                if (allWordsFound) Spacer(modifier = Modifier.height(16.dp))

                // ── ANSWER FORM ──────────────────────────────
                NotebookCard(jura = jura) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solve The Case",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TitleBrown,
                            fontFamily = jura
                        )
                        TextButton(onClick = { showAnswerForm = !showAnswerForm }) {
                            Text(
                                if (showAnswerForm) "Hide" else "Fill Answers",
                                fontFamily = jura,
                                color = BulletBrown,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Divider(color = BorderBeige2)

                    AnimatedVisibility(visible = showAnswerForm) {
                        Column {
                            Spacer(modifier = Modifier.height(16.dp))
                            CaseAnswerField("CULPRIT",  culpritInput, jura) { culpritInput = it.uppercase() }
                            Spacer(modifier = Modifier.height(12.dp))
                            CaseAnswerField("WEAPON",   weaponInput,  jura) { weaponInput  = it.uppercase() }
                            Spacer(modifier = Modifier.height(12.dp))
                            CaseAnswerField("SCENE",    sceneInput,   jura) { sceneInput   = it.uppercase() }
                            Spacer(modifier = Modifier.height(12.dp))
                            CaseAnswerField("MOTIVE",   motiveInput,  jura) { motiveInput  = it.uppercase() }
                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val case = CaseStorage.getCase()
                                    caseResult = case != null &&
                                            culpritInput.trim() == case.culprit.uppercase() &&
                                            weaponInput.trim()  == case.weapon.uppercase()  &&
                                            sceneInput.trim()   == case.scene.uppercase()   &&
                                            motiveInput.trim()  == case.motive.uppercase()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = TitleBrown),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(Icons.Default.Gavel, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Submit My Verdict",
                                    fontFamily = jura,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── RESULT CARD ──────────────────────────────
                val isCaseSolved = caseResult == true
                val showResult   = caseResult != null

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardCream),
                    border = BorderStroke(1.dp, BorderBeige)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Investigation Result 👉",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TitleBrown,
                            fontFamily = jura
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Clues: ${foundWords.size} / ${wordList.size} found" +
                                    if (allWordsFound) " — All clues uncovered!" else "",
                            color = if (allWordsFound) GreenSolved else BulletBrown,
                            fontFamily = jura,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )

                        if (showResult) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCaseSolved)
                                        "Case Solved Successfully"
                                    else
                                        "Not Correct. Keep Investigating.",
                                    fontSize = 16.sp,
                                    color = if (isCaseSolved) GreenSolved else RedUnsolved,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontFamily = jura,
                                    modifier = Modifier.weight(1f)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Button(
                                    onClick = {},
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isCaseSolved) GreenSolved else RedUnsolved
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isCaseSolved)
                                            Icons.Default.SentimentSatisfiedAlt
                                        else
                                            Icons.Default.SentimentDissatisfied,
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCaseSolved) "Case Solved" else "Not Solved",
                                        maxLines = 1,
                                        letterSpacing = 1.sp,
                                        fontFamily = jura,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PuzzleGrid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PuzzleGrid(
    puzzle: PuzzleData,
    foundWords: Map<String, List<Int>>,
    selectedCells: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    onWordFound: (String, List<Int>) -> Unit,
    jura: FontFamily
) {
    val foundCells = foundWords.values.flatten().toSet()

    fun handleCellTap(index: Int) {
        val newSel = if (selectedCells.contains(index)) {
            selectedCells - index
        } else {
            selectedCells + index
        }

        val match = puzzle.placements.firstOrNull { placement ->
            placement.positions.toSet() == newSel
        }

        if (match != null && !foundWords.containsKey(match.word)) {
            onWordFound(match.word, match.positions)
        } else {
            onSelectionChanged(newSel)
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(GRID_SIZE),
        modifier = Modifier
            .fillMaxWidth()
            .height(324.dp),
        verticalArrangement   = Arrangement.spacedBy(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        itemsIndexed(puzzle.grid) { index, letter ->
            val isFound    = foundCells.contains(index)
            val isSelected = selectedCells.contains(index)

            val bgColor = when {
                isFound    -> HighlightGreen
                isSelected -> HighlightYellow
                else       -> Color(0xFFEDD9BB)
            }

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) TitleBrown else BorderBeige2,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .clickable { handleCellTap(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = 14.sp,
                    fontWeight = if (isFound || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    fontFamily = jura,
                    color = if (isFound) GreenSolved else TitleBrown
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    if (selectedCells.isNotEmpty()) {
        TextButton(
            onClick = { onSelectionChanged(emptySet()) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Default.Clear,
                contentDescription = "Clear",
                tint = BulletBrown,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                "Clear Selection (${selectedCells.size} cells)",
                fontFamily = jura,
                color = BulletBrown,
                fontSize = 13.sp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CaseAnswerField
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CaseAnswerField(
    label: String,
    value: String,
    jura: FontFamily,
    onValueChange: (String) -> Unit
) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BulletBrown,
            fontFamily = jura,
            letterSpacing = 1.5.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text("Who / What / Where…", fontFamily = jura, color = BulletBrown.copy(alpha = 0.5f))
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = TitleBrown,
                unfocusedBorderColor = BorderBeige,
                focusedTextColor     = TitleBrown,
                unfocusedTextColor   = TitleBrown,
                cursorColor          = TitleBrown
            ),
            shape = RoundedCornerShape(12.dp),
            textStyle = LocalTextStyle.current.copy(
                fontFamily = jura,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  WordChipsGrid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun WordChipsGrid(
    words: List<String>,
    onRemove: (String) -> Unit,
    jura: FontFamily
) {
    val chunked = words.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        chunked.forEach { rowWords ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowWords.forEach { word ->
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = TitleBrown.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, TitleBrown.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = word,
                                fontFamily = jura,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TitleBrown
                            )
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = BulletBrown,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { onRemove(word) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Card shells
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun NotebookCard(
    jura: FontFamily,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 8.dp, start = 6.dp)
                .background(ShadowBrown, RoundedCornerShape(24.dp))
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 4.dp, bottom = 4.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream),
            border = BorderStroke(1.dp, BorderBeige),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), content = content)
        }
    }
}

@Composable
fun NotepadCard(
    jura: FontFamily,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 6.dp, start = 5.dp)
                .background(ShadowBrown2, RoundedCornerShape(18.dp))
        )
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream2),
            border = BorderStroke(1.dp, BorderBeige2)
        ) {
            Column(modifier = Modifier.padding(18.dp), content = content)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helper
// ─────────────────────────────────────────────────────────────────────────────
private fun addWord(
    text: String,
    current: List<String>,
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val w = text.trim().uppercase()
    when {
        w.isEmpty()          -> onError("Please enter a word.")
        w.length < 3         -> onError("Word must be at least 3 letters.")
        w.length > GRID_SIZE -> onError("Word too long (max $GRID_SIZE letters).")
        current.contains(w)  -> onError("\"$w\" is already in the list.")
        else                 -> onSuccess(current + w)
    }
}
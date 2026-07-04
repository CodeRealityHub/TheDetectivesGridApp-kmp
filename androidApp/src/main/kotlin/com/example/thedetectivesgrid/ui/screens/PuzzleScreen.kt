package com.example.thedetectivesgrid.ui.screens

import android.util.Log
import android.app.Activity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
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
import kotlinx.coroutines.delay

// ─────────────────────────────────────────────
//  Colour tokens
// ─────────────────────────────────────────────
private val BgSand          = Color(0xFFC2A574)
private val CardCream       = Color(0xFFF4E6D0)
private val CardCream2      = Color(0xFFF8EEDC)
private val BorderBeige     = Color(0xFFD9C2A0)
private val BorderBeige2    = Color(0xFFD7BE9A)
private val ShadowBrown     = Color(0xFF8D6E63)
private val ShadowBrown2    = Color(0xFFB08B62)
private val TitleBrown      = Color(0xFF3E2723)
private val SubBrown        = Color(0xFF5D4037)
private val TextBrown       = Color(0xFF4E342E)
private val BulletBrown     = Color(0xFF6D4C41)
private val GreenSolved     = Color(0xFF2E7D32)
private val RedUnsolved     = Color(0xFFC62828)
private val HighlightYellow = Color(0xFFFFE082)
private val HighlightGreen  = Color(0xFFA5D6A7)

private val MIN_WORDS = 13

@Composable
fun PuzzleScreen(
    caseNumber: String,
    onArchiveClick: () -> Unit,
    onPuzzleCreatorClick: () -> Unit
) {
    val jura = FontFamily(Font(R.font.jura))

    val focusManager       = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val view               = LocalView.current

    val dismissFocusRequester = remember { FocusRequester() }

    fun dismissKeyboardAndFocus() {
        dismissFocusRequester.requestFocus()
        keyboardController?.hide()
        (view.context as? Activity)?.window?.let { window ->
            WindowCompat.getInsetsController(window, view)
                .hide(WindowInsetsCompat.Type.ime())
        }
    }

    // ── State machine ──────────────────────────────────────
    var puzzleState by remember { mutableStateOf<PuzzleState>(PuzzleState.Input) }

    // ── Case answer fields (defined during input phase) ────
    var useDefaultCase by remember { mutableStateOf(true) }
    var culpritAnswer by remember { mutableStateOf("") }
    var weaponAnswer  by remember { mutableStateOf("") }
    var sceneAnswer   by remember { mutableStateOf("") }
    var motiveAnswer  by remember { mutableStateOf("") }

    // ── Word-input phase ────────────────────────────────────
    var wordInputText  by remember { mutableStateOf("") }
    var wordList       by remember { mutableStateOf(listOf<String>()) }
    var wordInputError by remember { mutableStateOf("") }

    // ── Generated puzzle ────────────────────────────────────
    var puzzleData by remember { mutableStateOf<PuzzleData?>(null) }

    // ── Grid selection ──────────────────────────────────────
    var selectedCells by remember { mutableStateOf(setOf<Int>()) }
    var foundWords    by remember { mutableStateOf(mapOf<String, List<Int>>()) }
    var gridSize      by remember { mutableStateOf(9) }

    // ── Answer inputs (guesses during playing phase) ────────
    var culpritInput   by remember { mutableStateOf("") }
    var weaponInput    by remember { mutableStateOf("") }
    var sceneInput     by remember { mutableStateOf("") }
    var motiveInput    by remember { mutableStateOf("") }
    var showAnswerForm by remember { mutableStateOf(false) }
    var caseResult     by remember { mutableStateOf<Boolean?>(null) }

    // ── Timer ───────────────────────────────────────────────
    var elapsedSeconds   by remember { mutableStateOf(0) }
    var isTimerRunning   by remember { mutableStateOf(false) }
    var showSolvedDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
        color = BgSand
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 46.dp)
        ) {
            // Invisible stable focus target
            Spacer(
                modifier = Modifier
                    .size(0.dp)
                    .focusRequester(dismissFocusRequester)
                    .focusTarget()
            )

            // ── TOP BAR ─────────────────────────────────────
            val headerEndReserve =
                if (puzzleState is PuzzleState.Playing && isTimerRunning) 195.dp else 100.dp

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = headerEndReserve)
                ) {
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

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (puzzleState is PuzzleState.Playing && isTimerRunning) {
                        TimerDisplay(
                            isRunning = isTimerRunning,
                            jura = jura,
                            onTick = { elapsedSeconds = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
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

            // ── SCROLLABLE CONTENT ───────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp)
            ) {

                // ══════════════════════════════════════════════
                //  PHASE 1 — WORD INPUT
                // ══════════════════════════════════════════════
                if (puzzleState is PuzzleState.Input) {

                    NotebookCard(jura = jura) {

                        // ── CASE DETAILS SECTION ─────────────
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Case Details",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TitleBrown,
                                    fontFamily = jura
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (useDefaultCase)
                                        "Using preloaded case answers"
                                    else
                                        "Set the answers the player must guess",
                                    fontSize = 13.sp,
                                    color = BulletBrown,
                                    fontFamily = jura
                                )
                            }
                            Switch(
                                checked = useDefaultCase,
                                onCheckedChange = { defaultChecked ->
                                    useDefaultCase = defaultChecked
                                    if (useDefaultCase) {
                                        // switching back to preloaded — clear custom fields
                                        culpritAnswer = ""
                                        weaponAnswer  = ""
                                        sceneAnswer   = ""
                                        motiveAnswer  = ""
                                        wordInputError = ""
                                    }
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor   = Color.White,
                                    checkedTrackColor   = TitleBrown,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BulletBrown.copy(alpha = 0.4f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ── PRELOADED CASE PREVIEW ───────────
                        AnimatedVisibility(visible = useDefaultCase) {
                            Column {
                                PreloadedCasePreview(case = CaseStorage.getDefaultCase(), jura = jura)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }

                        // ── CUSTOM CASE FIELDS ───────────────
                        AnimatedVisibility(visible = !useDefaultCase) {
                            Column {
                                CaseAnswerField(
                                    label = "CULPRIT",
                                    value = culpritAnswer,
                                    jura = jura
                                ) { culpritAnswer = it.uppercase() }

                                Spacer(modifier = Modifier.height(10.dp))

                                CaseAnswerField(
                                    label = "WEAPON",
                                    value = weaponAnswer,
                                    jura = jura
                                ) { weaponAnswer = it.uppercase() }

                                Spacer(modifier = Modifier.height(10.dp))

                                CaseAnswerField(
                                    label = "SCENE",
                                    value = sceneAnswer,
                                    jura = jura
                                ) { sceneAnswer = it.uppercase() }

                                Spacer(modifier = Modifier.height(10.dp))

                                CaseAnswerField(
                                    label = "MOTIVE",
                                    value = motiveAnswer,
                                    jura = jura
                                ) { motiveAnswer = it.uppercase() }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Divider(color = BorderBeige2)
                        Spacer(modifier = Modifier.height(16.dp))

                        // ── WORDS SECTION ────────────────────
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
                                    Text(
                                        "Enter a word…",
                                        fontFamily = jura,
                                        color = BulletBrown.copy(alpha = 0.6f)
                                    )
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
                                // Validate case details first (only when custom case is used)
                                if (!useDefaultCase) {
                                    if (culpritAnswer.isBlank() || weaponAnswer.isBlank() ||
                                        sceneAnswer.isBlank()   || motiveAnswer.isBlank()
                                    ) {
                                        wordInputError = "Please fill in Culprit, Weapon, Scene and Motive."
                                        return@Button
                                    }
                                }

                                if (wordList.size < MIN_WORDS) {
                                    wordInputError = "Please add at least $MIN_WORDS words."
                                    return@Button
                                }

                                focusManager.clearFocus(force = true)
                                dismissKeyboardAndFocus()

                                Log.d("WORDS", "Count = ${wordList.size}")
                                Log.d("WORDS", "Total letters = ${wordList.sumOf { it.length }}")
                                wordList.forEachIndexed { index, word ->
                                    Log.d("WORDS", "$index -> '$word' (${word.length})")
                                }

                                val generated = PuzzleGenerators.generate(wordList)
                                Log.d("PUZZLE", "Grid Size = ${generated.gridSize}")
                                Log.d("PUZZLE", "Cell Count = ${generated.grid.size}")

                                if (generated == null) {
                                    wordInputError = "Could not place all words. Try different words."
                                } else {
                                    puzzleData = generated
                                    gridSize   = generated.gridSize

                                    // Save either the preloaded case or the user-defined one
                                    if (useDefaultCase) {
                                        CaseStorage.resetToDefault()
                                    } else {
                                        CaseStorage.saveCase(
                                            CaseData(
                                                culprit = culpritAnswer.trim(),
                                                weapon  = weaponAnswer.trim(),
                                                scene   = sceneAnswer.trim(),
                                                motive  = motiveAnswer.trim()
                                            )
                                        )
                                    }

                                    puzzleState      = PuzzleState.Playing
                                    foundWords       = emptyMap()
                                    showAnswerForm   = false
                                    caseResult       = null
                                    culpritInput     = ""
                                    weaponInput      = ""
                                    sceneInput       = ""
                                    motiveInput      = ""
                                    elapsedSeconds   = 0
                                    isTimerRunning   = true
                                    showSolvedDialog = false
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = wordList.size >= MIN_WORDS,
                            colors = ButtonDefaults.buttonColors(
                                containerColor          = TitleBrown,
                                disabledContainerColor  = TitleBrown.copy(alpha = 0.4f)
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

                    Spacer(modifier = Modifier.height(16.dp))
                    HowToPlayCard(jura = jura)
                }

                // ══════════════════════════════════════════════
                //  PHASE 2 — PLAYING
                // ══════════════════════════════════════════════
                if (puzzleState is PuzzleState.Playing && puzzleData != null) {
                    val puzzle = puzzleData!!

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
                                    puzzleState    = PuzzleState.Input
                                    wordList       = emptyList()
                                    wordInputText  = ""
                                    wordInputError = ""
                                    useDefaultCase = true
                                    culpritAnswer  = ""
                                    weaponAnswer   = ""
                                    sceneAnswer    = ""
                                    motiveAnswer   = ""
                                    isTimerRunning = false
                                    elapsedSeconds = 0
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

                        PuzzleGrid(
                            puzzle             = puzzle,
                            gridSize           = gridSize,
                            foundWords         = foundWords,
                            selectedCells      = selectedCells,
                            onSelectionChanged = { cells -> selectedCells = cells },
                            onWordFound        = { word, positions ->
                                foundWords    = foundWords + (word to positions)
                                selectedCells = emptySet()
                            },
                            jura = jura
                        )

                        Spacer(modifier = Modifier.height(18.dp))

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

                    Spacer(modifier = Modifier.height(16.dp))
                    HowToPlayCard(jura = jura)
                    Spacer(modifier = Modifier.height(24.dp))

                    // ── ALL WORDS FOUND BANNER ───────────────────
                    val allWordsFound = foundWords.size == wordList.size
                    AnimatedVisibility(
                        visible = allWordsFound,
                        enter   = fadeIn() + slideInVertically(),
                        exit    = fadeOut()
                    ) {
                        Card(
                            shape  = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = GreenSolved.copy(alpha = 0.15f)
                            ),
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

                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            // Header
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

                                if (useDefaultCase) {
                                    Text(
                                        text = "Using default case answers.",
                                        fontFamily = jura,
                                        color = BulletBrown,
                                        fontSize = 13.sp
                                    )
                                } else {
                                    TextButton(
                                        onClick = { showAnswerForm = !showAnswerForm }
                                    ) {
                                        Text(
                                            text = if (showAnswerForm) "Hide" else "Fill Answers",
                                            fontFamily = jura,
                                            color = BulletBrown,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = showAnswerForm && !useDefaultCase
                            ) {

                                Column {

                                    Spacer(modifier = Modifier.height(16.dp))

                                    CaseAnswerField(
                                        label = "CULPRIT",
                                        value = culpritInput,
                                        jura = jura
                                    ) { culpritInput = it.uppercase() }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CaseAnswerField(
                                        label = "WEAPON",
                                        value = weaponInput,
                                        jura = jura
                                    ) { weaponInput = it.uppercase() }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CaseAnswerField(
                                        label = "SCENE",
                                        value = sceneInput,
                                        jura = jura
                                    ) { sceneInput = it.uppercase() }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    CaseAnswerField(
                                        label = "MOTIVE",
                                        value = motiveInput,
                                        jura = jura
                                    ) { motiveInput = it.uppercase() }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Button(
                                onClick = {
                                    val case = CaseStorage.getCase()

                                    if (useDefaultCase) {
                                        case?.let {
                                            culpritInput = it.culprit
                                            weaponInput = it.weapon
                                            sceneInput = it.scene
                                            motiveInput = it.motive
                                        }
                                    }

                                    caseResult =
                                        case != null &&
                                                culpritInput.trim().equals(case.culprit.trim(), ignoreCase = true) &&
                                                weaponInput.trim().equals(case.weapon.trim(), ignoreCase = true) &&
                                                sceneInput.trim().equals(case.scene.trim(), ignoreCase = true) &&
                                                motiveInput.trim().equals(case.motive.trim(), ignoreCase = true)

                                    if (caseResult == true) {
                                        isTimerRunning = false
                                        showSolvedDialog = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = TitleBrown
                                ),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 14.dp)
                            ) {
                                Icon(
                                    Icons.Default.Gavel,
                                    contentDescription = null,
                                    tint = Color.White
                                )

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "SUBMIT VERDICT",
                                    color = Color.White,
                                    fontFamily = jura,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── RESULT CARD ──────────────────────────────
                    val isCaseSolved = caseResult == true
                    val showResult   = caseResult != null

                    Card(
                        shape  = RoundedCornerShape(16.dp),
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
                                        contentPadding = PaddingValues(
                                            horizontal = 14.dp,
                                            vertical = 10.dp
                                        )
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
                } // end PHASE 2
            } // end scrollable Column
        } // end outer Column
    } // end Surface

    // ── Case Solved Dialog ──────────────────────────────────
    if (showSolvedDialog) {
        AlertDialog(
            onDismissRequest = { showSolvedDialog = false },
            confirmButton = {
                TextButton(onClick = { showSolvedDialog = false }) {
                    Text(
                        "Close",
                        fontFamily = jura,
                        fontWeight = FontWeight.Bold,
                        color = TitleBrown
                    )
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = GreenSolved,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    "Case Solved!",
                    fontFamily = jura,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = TitleBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    "Solved in ${formatElapsed(elapsedSeconds)}",
                    fontFamily = jura,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SubBrown,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            containerColor = CardCream,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PreloadedCasePreview
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PreloadedCasePreview(
    case: CaseData,
    jura: FontFamily
) {
    Surface(
        shape  = RoundedCornerShape(12.dp),
        color  = TitleBrown.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, BorderBeige)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            PreviewRow("CULPRIT", case.culprit, jura)
            Spacer(modifier = Modifier.height(6.dp))
            PreviewRow("WEAPON", case.weapon, jura)
            Spacer(modifier = Modifier.height(6.dp))
            PreviewRow("SCENE", case.scene, jura)
            Spacer(modifier = Modifier.height(6.dp))
            PreviewRow("MOTIVE", case.motive, jura)
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String, jura: FontFamily) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = BulletBrown,
            fontFamily = jura,
            letterSpacing = 1.5.sp
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = TitleBrown,
            fontFamily = jura
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  TimerDisplay
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TimerDisplay(
    isRunning: Boolean,
    jura: FontFamily,
    onTick: (Int) -> Unit = {}
) {
    var elapsedSeconds by remember(isRunning) { mutableStateOf(0) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000)
            elapsedSeconds++
            onTick(elapsedSeconds)
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardCream)
            .border(1.dp, BorderBeige, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Timer,
            contentDescription = "Timer",
            tint = TitleBrown,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = formatElapsed(elapsedSeconds),
            fontFamily = jura,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = TitleBrown
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  PuzzleGrid
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun PuzzleGrid(
    puzzle: PuzzleData,
    gridSize: Int,
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

    val screenWidth           = LocalConfiguration.current.screenWidthDp.dp
    val totalHorizontalPadding = 76.dp
    val spacing               = 3.dp
    val cellSize              = (screenWidth - totalHorizontalPadding - spacing * (gridSize - 1)) / gridSize

    val fontSize = when {
        gridSize >= 20 -> 6.sp
        gridSize >= 17 -> 8.sp
        gridSize >= 14 -> 10.sp
        gridSize >= 11 -> 12.sp
        else           -> 13.sp
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(gridSize),
        modifier = Modifier
            .fillMaxWidth()
            .height((cellSize + spacing) * gridSize - spacing),
        verticalArrangement   = Arrangement.spacedBy(spacing),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        userScrollEnabled = false
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
                    .size(cellSize)
                    .clip(RoundedCornerShape(4.dp))
                    .background(bgColor)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) TitleBrown else BorderBeige2,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .clickable { handleCellTap(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontSize = fontSize,
                    fontWeight = if (isFound || isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                    fontFamily = jura,
                    color = if (isFound) GreenSolved else TitleBrown,
                    textAlign = TextAlign.Center
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
    enabled: Boolean = true,
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
            enabled = enabled,
            placeholder = {
                Text(
                    "Who / What / Where…",
                    fontFamily = jura,
                    color = BulletBrown.copy(alpha = 0.5f)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = TitleBrown,
                unfocusedBorderColor = BorderBeige,
                focusedTextColor     = TitleBrown,
                unfocusedTextColor   = TitleBrown,
                cursorColor          = TitleBrown,
                disabledBorderColor  = BorderBeige.copy(alpha = 0.5f),
                disabledTextColor    = TitleBrown.copy(alpha = 0.4f)
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
                        shape  = RoundedCornerShape(20.dp),
                        color  = TitleBrown.copy(alpha = 0.12f),
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
            shape  = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardCream2),
            border = BorderStroke(1.dp, BorderBeige2)
        ) {
            Column(modifier = Modifier.padding(18.dp), content = content)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  HowToPlayCard
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun HowToPlayCard(jura: FontFamily) {
    var expanded by remember { mutableStateOf(false) }

    val hints = listOf(
        "🔍" to "Tap individual letters in the grid to select them.",
        "✅" to "Select all letters of a hidden word to mark it found.",
        "↩️" to "Tap a selected cell again to deselect it, or use Clear Selection.",
        "📋" to "All words to find are listed in the Words To Find card below the grid.",
        "🕵️" to "Once all words are found, fill in Culprit, Weapon, Scene & Motive to solve the case.",
        "💡" to "Words can be hidden horizontally, vertically, or diagonally in any direction.",
        "📐" to "The grid size adjusts automatically based on the number and length of words you add — more words means a bigger grid."
    )

    NotebookCard(jura = jura) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = BulletBrown,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "How To Play",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = jura,
                    color = TitleBrown
                )
            }
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp
                else          Icons.Default.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = BulletBrown,
                modifier = Modifier.size(20.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderBeige2)
                Spacer(modifier = Modifier.height(12.dp))
                hints.forEach { (emoji, text) ->
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(text = emoji, fontSize = 15.sp)
                        Text(
                            text = text,
                            fontSize = 13.sp,
                            fontFamily = jura,
                            color = TextBrown,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  Helpers
// ─────────────────────────────────────────────────────────────────────────────
private fun formatElapsed(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

private const val MAX_WORD_LENGTH = 15
private fun addWord(
    text: String,
    current: List<String>,
    onSuccess: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val w = text.trim().uppercase()
    when {
        w.isEmpty()              -> onError("Please enter a word.")
        w.length < 3             -> onError("Word must be at least 3 letters.")
        w.length > MAX_WORD_LENGTH -> onError("Word too long (max $MAX_WORD_LENGTH letters).")
        current.contains(w)      -> onError("\"$w\" is already in the list.")
        else                     -> onSuccess(current + w)
    }
}
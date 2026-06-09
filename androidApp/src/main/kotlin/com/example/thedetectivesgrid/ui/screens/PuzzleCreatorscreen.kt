package com.example.thedetectivesgrid.ui.screens


import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesomeMosaic
import androidx.compose.material.icons.filled.BatchPrediction
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.FolderCopy
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thedetectivesgrid.generator.PuzzleGenerator
import com.example.thedetectivesgrid.models.PuzzleState
import com.example.thedetectivesgrid.ui.screens.components.PuzzleCell
import androidx.compose.ui.text.style.TextDecoration
import com.example.thedetectivesgrid.models.CaseData
import com.example.thedetectivesgrid.models.CaseStorage
import com.example.thedetectivesgrid.models.WordPlacement

@Composable
fun PuzzleCreatorScreen(
    onBack: () -> Unit,
    onGoToPuzzleScreen: () -> Unit,
    onGoToArchiveScreen: () -> Unit
) {

    val juraFont = FontFamily(
        Font(com.example.thedetectivesgrid.R.font.jura)
    )

    var state by remember {
        mutableStateOf<PuzzleState>(PuzzleState.Input)
    }

    var culprit by remember { mutableStateOf("") }
    var weapon by remember { mutableStateOf("") }
    var scene by remember { mutableStateOf("") }
    var motive by remember { mutableStateOf("") }
    var wordInput by remember { mutableStateOf("") }

    var culpritGuess by remember {
        mutableStateOf("")
    }

    var weaponGuess by remember {
        mutableStateOf("")
    }

    var sceneGuess by remember {
        mutableStateOf("")
    }

    var motiveGuess by remember {
        mutableStateOf("")
    }

    var caseSolved by remember {
        mutableStateOf(false)
    }


    var generatedGrid by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var placements by remember {
        mutableStateOf<List<WordPlacement>>(emptyList())
    }

    val solvedWords = remember {
        mutableStateListOf<String>()
    }

    val wordList = wordInput
        .split(",")
        .map { it.trim().uppercase() }
        .filter { it.isNotEmpty() }


    val selectedCells = remember {
        mutableStateListOf<Int>()
    }

    val foundCells = remember {
        mutableStateListOf<Int>()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFC2A574)
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp)
        ) {


            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {

                IconButton(
                    onClick = {
                        onBack()
                    },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Back",
                        tint = Color(0xFF3E2723),
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Puzzle Creator Mode",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = juraFont,
                    color = Color(0xFF3E2723),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 60.dp, end = 90.dp, top = 13.dp),
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    IconButton(
                        onClick = {
                            onGoToPuzzleScreen()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesomeMosaic,
                            contentDescription = "Puzzle Screen",
                            tint = Color(0xFF3E2723),
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            onGoToArchiveScreen()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderCopy,
                            contentDescription = "Archive",
                            tint = Color(0xFF3E2723),
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),

                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(20.dp))


                if (state is PuzzleState.Input) {

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF4E6D0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),

                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {

                            Text(
                                text = "Enter Words For Puzzle",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                fontFamily = juraFont,
                                color = Color(0xFF3E2723)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = culprit,
                                onValueChange = {
                                    culprit = it.uppercase()
                                },

                                label = {
                                    Text(
                                        "Culprit",
                                        fontFamily = juraFont
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Person",
                                        tint = Color(0xFF5D4037)
                                    )
                                },

                                textStyle = TextStyle(
                                    fontFamily = juraFont,
                                    fontSize = 18.sp,
                                    color = Color(0xFF3E2723),
                                    fontWeight = FontWeight.Bold
                                ),

                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = weapon,
                                onValueChange = {
                                    weapon = it.uppercase()
                                },

                                label = {
                                    Text(
                                        "Weapon",
                                        fontFamily = juraFont
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = "weapon",
                                        tint = Color(0xFF5D4037)
                                    )
                                },

                                textStyle = TextStyle(
                                    fontFamily = juraFont,
                                    fontSize = 18.sp,
                                    color = Color(0xFF3E2723),
                                    fontWeight = FontWeight.Bold
                                ),

                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = scene,
                                onValueChange = {
                                    scene = it.uppercase()
                                },

                                label = {
                                    Text(
                                        "Scene",
                                        fontFamily = juraFont
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = "Scene",
                                        tint = Color(0xFF5D4037)
                                    )
                                },

                                textStyle = TextStyle(
                                    fontFamily = juraFont,
                                    fontSize = 18.sp,
                                    color = Color(0xFF3E2723),
                                    fontWeight = FontWeight.Bold
                                ),

                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = motive,
                                onValueChange = {
                                    motive = it.uppercase()
                                },

                                label = {
                                    Text(
                                        "Motive",
                                        fontFamily = juraFont
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.TheaterComedy,
                                        contentDescription = "Motive",
                                        tint = Color(0xFF5D4037)
                                    )
                                },

                                textStyle = TextStyle(
                                    fontFamily = juraFont,
                                    fontSize = 18.sp,
                                    color = Color(0xFF3E2723),
                                    fontWeight = FontWeight.Bold
                                ),

                                modifier = Modifier.fillMaxWidth(),
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            OutlinedTextField(
                                value = wordInput,
                                onValueChange = {
                                    wordInput = it.uppercase()
                                },

                                label = {
                                    Text(
                                        "Enter Words (comma separated)",
                                        fontFamily = juraFont
                                    )
                                },

                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.BatchPrediction,
                                        contentDescription = "Words",
                                        tint = Color(0xFF5D4037)
                                    )
                                },

                                textStyle = TextStyle(
                                    fontFamily = juraFont,
                                    fontSize = 18.sp,
                                    color = Color(0xFF3E2723),
                                    fontWeight = FontWeight.Bold
                                ),

                                modifier = Modifier.fillMaxWidth(),

                                minLines = 3
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    if (culprit.isNotEmpty() &&
                                        weapon.isNotEmpty() &&
                                        scene.isNotEmpty() &&
                                        motive.isNotEmpty() &&
                                        wordList.isNotEmpty()) {
                                        val puzzleData = PuzzleGenerator.generate(wordList)
                                        generatedGrid = puzzleData.grid
                                        placements = puzzleData.placements
                                        CaseStorage.saveCase(
                                            CaseData(
                                                culprit = culprit,
                                                weapon = weapon,
                                                scene = scene,
                                                motive = motive
                                            )
                                        )
                                        println("SAVING CASE")
                                        println("CULPRIT = $culprit")
                                        println("WEAPON = $weapon")
                                        println("SCENE = $scene")
                                        println("MOTIVE = $motive")
                                        state = PuzzleState.Playing
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {

                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Default.PlayCircleOutline,
                                        contentDescription = "Generate Puzzle",
                                        modifier = Modifier.size(20.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = "Generate Puzzle & Start Play",
                                        fontFamily = juraFont,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                // ✅ Spacer added after CreatorHowToPlayCard in Input state
                if (state is PuzzleState.Input) {
                    Spacer(modifier = Modifier.height(16.dp))
                    CreatorHowToPlayCard(juraFont = juraFont)
                    Spacer(modifier = Modifier.height(80.dp))
                }

                if (state is PuzzleState.Playing) {

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF4E6D0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = "Puzzle Grid",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = juraFont,
                                color = Color(0xFF3E2723)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(9),
                                modifier = Modifier.height(360.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {

                                itemsIndexed(generatedGrid) { index, letter ->

                                    PuzzleCell(
                                        letter = letter,
                                        isSelected =
                                            index in selectedCells ||
                                                    index in foundCells,
                                        onClick = {
                                            if (index in selectedCells) {
                                                selectedCells.remove(index)
                                            } else {
                                                selectedCells.add(index)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    CreatorHowToPlayCard(juraFont = juraFont)

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = {

                            val selected =
                                selectedCells.sorted()

                            val matchedPlacement =
                                placements.firstOrNull {
                                    it.positions.sorted() == selected
                                }

                            if (
                                matchedPlacement != null &&
                                matchedPlacement.word !in solvedWords
                            ) {

                                solvedWords.add(
                                    matchedPlacement.word
                                )

                                foundCells.addAll(
                                    matchedPlacement.positions
                                )
                            }
                            println("SELECTED = ${selectedCells.sorted()}")
                            println("PLACEMENTS = ${placements.map { it.word to it.positions }}")
                            selectedCells.clear()
                        }
                    )
                    {
                        Text("Check Word")
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF4E6D0)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {

                            Text(
                                text = "Words To Find",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = juraFont,
                                color = Color(0xFF3E2723)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            wordList.forEach { word ->

                                Text(
                                    text = "* $word",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = juraFont,
                                    color = Color(0xFF4E342E),
                                    textDecoration =
                                        if (word in solvedWords)
                                            TextDecoration.LineThrough
                                        else
                                            TextDecoration.None
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = culpritGuess,
                                onValueChange = {
                                    culpritGuess = it.uppercase()
                                },
                                label = {
                                    Text("Enter Culprit")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = weaponGuess,
                                onValueChange = {
                                    weaponGuess = it.uppercase()
                                },
                                label = {
                                    Text("Enter Weapon")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = sceneGuess,
                                onValueChange = {
                                    sceneGuess = it.uppercase()
                                },
                                label = {
                                    Text("Enter Scene")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        ) {
                            OutlinedTextField(
                                value = motiveGuess,
                                onValueChange = {
                                    motiveGuess = it.uppercase()
                                },
                                label = {
                                    Text("Enter Motive")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(35.dp))

                    if (state is PuzzleState.Playing) {
                        val allCluesSolved =

                            solvedWords.size == wordList.size
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF4E6D0)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {

                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {

                                Text(
                                    text = "Investigation Result 👉",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = juraFont,
                                    color = Color(0xFF3E2723)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                val allWordsFound = solvedWords.size == wordList.size

                                Text(
                                    text =
                                        if (allWordsFound)
                                            "All Clues Found. Solve The Case."
                                        else
                                            "${solvedWords.size}/${wordList.size} Clues Found",
                                    fontSize = 18.sp,
                                    color =
                                        if (allWordsFound)
                                            Color(0xFF2E7D32)
                                        else
                                            Color(0xFFEF6C00),
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = juraFont
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(35.dp))

                    Button(
                        onClick = {

                            caseSolved =
                                culpritGuess == culprit &&
                                        weaponGuess == weapon &&
                                        sceneGuess == scene &&
                                        motiveGuess == motive
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Solve Case")
                    }

                    Spacer(modifier = Modifier.height(21.dp))

                    if (caseSolved) {

                        Text(
                            text = "CASE SOLVED ✅",
                            color = Color(0xFF2E7D32),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                    } else {

                        Text(
                            text = "Case Not Solved Yet ❌",
                            color = Color.Red,
                            fontSize = 18.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(80.dp)) // 👈 add this
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
//  CreatorHowToPlayCard — collapsible hints panel for creator screen
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun CreatorHowToPlayCard(juraFont: FontFamily) {
    var expanded by remember { mutableStateOf(false) }

    val inputHints = listOf(
        "✏️" to "Fill in Culprit, Weapon, Scene and Motive — these are the answers players must guess.",
        "📝" to "Enter all hidden words separated by commas (e.g. KNIFE, BUTLER, LIBRARY).",
        "🔡" to "Words are auto-uppercased. Each word must be 3–9 letters.",
        "🎮" to "Tap Generate Puzzle & Start Play to preview the puzzle as a player would see it.",
        "💾" to "The case answers are saved automatically for players to verify against."
    )

    val playHints = listOf(
        "🔍" to "Tap letters in the grid to select them. Select all letters of a word to mark it found.",
        "↩️" to "Tap a selected letter again to deselect it.",
        "📋" to "Track your progress in the Words To Find list below the grid.",
        "🕵️" to "Once all words are found, enter your guesses and tap Solve Case.",
        "💡" to "Words can run horizontally, vertically, or diagonally in any direction."
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF4E6D0)),
        modifier = androidx.compose.ui.Modifier.fillMaxWidth()
    ) {
        Column(modifier = androidx.compose.ui.Modifier.padding(16.dp)) {
            Row(
                modifier = androidx.compose.ui.Modifier
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
                        imageVector = Icons.Default.PlayCircleOutline,
                        contentDescription = null,
                        tint = Color(0xFF6D4C41),
                        modifier = androidx.compose.ui.Modifier.size(18.dp)
                    )
                    Text(
                        text = "How To Play",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = juraFont,
                        color = Color(0xFF3E2723)
                    )
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = Color(0xFF6D4C41),
                    modifier = androidx.compose.ui.Modifier.size(20.dp)
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                    Divider(color = Color(0xFFD7BE9A))
                    Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                    Text(
                        text = "Setup",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = juraFont,
                        color = Color(0xFF6D4C41),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                    inputHints.forEach { (emoji, text) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = androidx.compose.ui.Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                fontFamily = juraFont,
                                color = Color(0xFF4E342E),
                                fontWeight = FontWeight.Bold,
                                modifier = androidx.compose.ui.Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                    Divider(color = Color(0xFFD7BE9A))
                    Spacer(modifier = androidx.compose.ui.Modifier.height(10.dp))
                    Text(
                        text = "Playing",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = juraFont,
                        color = Color(0xFF6D4C41),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.height(6.dp))
                    playHints.forEach { (emoji, text) ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = androidx.compose.ui.Modifier.padding(vertical = 3.dp)
                        ) {
                            Text(text = emoji, fontSize = 14.sp)
                            Text(
                                text = text,
                                fontSize = 13.sp,
                                fontFamily = juraFont,
                                color = Color(0xFF4E342E),
                                fontWeight = FontWeight.Bold,
                                modifier = androidx.compose.ui.Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}
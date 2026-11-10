package com.example.thedetectivesgrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thedetectivesgrid.ui.screens.PuzzleScreen
import com.example.thedetectivesgrid.ui.screens.CaseArchiveScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thedetectivesgrid.ui.screens.PuzzleCreatorScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            var showSplash by remember {
                mutableStateOf(true)
            }

            if (showSplash) {

                DetectiveSplashScreen(
                    onFinished = {
                        showSplash = false
                    }
                )

            } else {

                DetectiveApp()

            }
        }
    }
}


@Composable
fun DetectiveSplashScreen(
    onFinished: () -> Unit
) {

    LaunchedEffect(Unit) {
        delay(2000)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF8050C8),
                        Color(0xFF57269B),
                        Color(0xFF301078)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(
                    R.drawable.word_search_dagger_icon
                ),
                contentDescription = "Detective Grid Logo",
                modifier = Modifier.size(220.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "THE DETECTIVE",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "GRID GAME",
                color = Color(0xFFFFD966),
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
fun DetectiveApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "puzzle/1"
    ) {

        composable(
            route = "puzzle/{caseNumber}"
        ) { backStackEntry ->

            val caseNumber =
                backStackEntry.arguments?.getString("caseNumber") ?: "1"

            PuzzleScreen(
                caseNumber = caseNumber,
                onArchiveClick = {
                    navController.navigate("archive")
                },
                onPuzzleCreatorClick = {
                    navController.navigate("creator")
                }
            )
        }

        composable("archive") {
            CaseArchiveScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onCaseClick = { caseNumber ->
                    navController.navigate("puzzle/$caseNumber")
                }
            )
        }

        composable("creator") {
            PuzzleCreatorScreen(
                onBack = {
                    navController.popBackStack()
                },
                onGoToPuzzleScreen = {
                    navController.navigate("puzzle/1")
                },
                onGoToArchiveScreen = {
                    navController.navigate("archive")
                }
            )
        }
    }
}




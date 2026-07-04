package com.example.thedetectivesgrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.thedetectivesgrid.ui.screens.PuzzleScreen
import com.example.thedetectivesgrid.ui.screens.CaseArchiveScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.thedetectivesgrid.ui.screens.PuzzleCreatorScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DetectiveApp()
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




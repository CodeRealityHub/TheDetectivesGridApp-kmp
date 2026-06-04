package com.example.thedetectivesgrid.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import com.example.thedetectivesgrid.models.CaseArchive
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CaseArchive(
    val caseNumber: String,
    val isUnlocked: Boolean
)

@Composable
fun CaseArchiveScreen(
    onBackClick: () -> Unit,
    onCaseClick: (String) -> Unit
) {

    val archiveCases = (1..30).map {
        CaseArchive(it.toString())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFAC9265))
            .verticalScroll(rememberScrollState())
            .padding(vertical = 20.dp)
    ) {

        // TOP BAR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    start = 12.dp,
                    end = 12.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // BACK BUTTON
            IconButton(
                onClick = {
                    onBackClick()
                }
            ) {

                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "Back",
                    tint = Color(0xFF1C1C1C),
                    modifier = Modifier.size(30.dp)
                )
            }

            // TITLE
            Text(
                text = "CASE ARCHIVE",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0E0D0C),
                fontFamily = FontFamily(Font(com.example.thedetectivesgrid.R.font.jura))
            )

            // EMPTY SPACE FOR BALANCE
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(18.dp))

        // UNLOCKED CASES
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(1002.dp),
            userScrollEnabled = false,
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            items(archiveCases) { case ->

                ArchiveFolderItem(
                    caseNumber = case.caseNumber,
                    onClick = {
                        onCaseClick(case.caseNumber)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Composable
fun ArchiveFolderItem(
    caseNumber: String,
    onClick: () -> Unit
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Card(
            modifier = Modifier
                .size(
                    width = 90.dp,
                    height = 70.dp
                )
                .clickable {
                    onClick()
                },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFDDB178)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {

            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .padding(start = 46.dp)
                        .size(
                            width = 28.dp,
                            height = 10.dp
                        )
                        .background(
                            Color(0xFFECC692),
                            RoundedCornerShape(
                                topStart = 6.dp,
                                topEnd = 6.dp
                            )
                        )
                )

                Text(
                    text = "CASE",
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.Black,
                    fontFamily = FontFamily(
                        Font(com.example.thedetectivesgrid.R.font.jura)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = caseNumber,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF232121),
            fontFamily = FontFamily(
                Font(com.example.thedetectivesgrid.R.font.jura)
            )
        )
    }
}


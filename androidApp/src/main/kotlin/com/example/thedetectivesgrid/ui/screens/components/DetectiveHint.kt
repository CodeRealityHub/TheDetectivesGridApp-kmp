package com.example.thedetectivesgrid.ui.screens.components

import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.example.thedetectivesgrid.R

// Detective game colour palette
private val HintCream = Color(0xFFF4E6D0)
private val HintBorder = Color(0xFFD9C2A0)
private val HintBrown = Color(0xFF3E2723)
private val HintSubBrown = Color(0xFF6D4C41)

// ----------------------------------------------------
// REUSABLE DETECTIVE HINT DIALOG
// ----------------------------------------------------

@Composable
fun DetectiveHintDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    jura: FontFamily,
    rules: List<String> = HintRules.puzzleScreen,
    title: String = "PUZZLE RULES",
    subtitle: String = "Before starting your investigation"
) {
    if (!show) return

    Dialog(
        onDismissRequest = onDismiss
    ) {
        // Keep the keyboard hidden while the hint is open
        val dialogView = LocalView.current
        SideEffect {
            ((dialogView as? DialogWindowProvider) ?: (dialogView.parent as? DialogWindowProvider))
                ?.window
                ?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = HintCream,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                HintBorder
            ),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // Header: Detective + title + circular app logo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🕵️‍♂️",
                        fontSize = 38.sp
                    )

                    Spacer(modifier = Modifier.width(9.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Detective's Hint",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = jura,
                            color = HintBrown
                        )

                        Text(
                            text = subtitle,
                            fontSize = 10.sp,
                            fontFamily = jura,
                            color = HintSubBrown
                        )
                    }

                    // Circular app logo from drawable
                    Image(
                        painter = painterResource(
                            id = R.drawable.word_search_dagger_icon
                        ),
                        contentDescription = "The Detective's Grid Logo",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(
                                width = 2.dp,
                                color = HintSubBrown,
                                shape = CircleShape
                            )
                            .padding(3.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                HorizontalDivider(color = HintBorder)

                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = jura,
                    color = HintBrown
                )

                rules.forEachIndexed { index, rule ->
                    HintRule(
                        number = "${index + 1}.",
                        text = rule,
                        jura = jura
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HintBrown
                    ),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text(
                        text = "GOT IT!",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = jura,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------
// REUSABLE RULE ROW
// ----------------------------------------------------

@Composable
private fun HintRule(
    number: String,
    text: String,
    jura: FontFamily
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text(
            text = number,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = jura,
            color = HintBrown
        )

        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = jura,
            color = HintSubBrown,
            lineHeight = 19.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

// ----------------------------------------------------
// REUSABLE SMALL HINT BUTTON
// ----------------------------------------------------

@Composable
fun DetectiveHintButton(
    onClick: () -> Unit,
    jura: FontFamily,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        contentPadding = PaddingValues(
            horizontal = 9.dp,
            vertical = 4.dp
        )
    ) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = "Show hint",
            tint = HintBrown,
            modifier = Modifier.size(16.dp)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = "HINT",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = jura,
            color = HintBrown
        )
    }
}
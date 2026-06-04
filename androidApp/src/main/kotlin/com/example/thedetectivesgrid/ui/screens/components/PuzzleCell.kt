package com.example.thedetectivesgrid.ui.screens.components

import android.text.Layout
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PuzzleCell(
   letter: String,
   isSelected: Boolean,
   onClick: () -> Unit
) {
    val backgroundColor =
        if (isSelected) Color(0xFF8D6E63)
        else Color(0xFFEAD7B7)

    val textColor =
        if (isSelected) Color.White
        else Color(0xFF3E2723)

    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                backgroundColor,
                RoundedCornerShape(4.dp)
            )
            .border(
                1.dp,
                Color(0xFF8D6E63)
            ).clickable{
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}
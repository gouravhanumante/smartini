package com.dailyrounds.quizapp.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OptionItem(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean,
    isInCorrect: Boolean,
    onClick: () -> Unit
) {

    val backgroundColor = when {
        isCorrect -> Color(0xFF4CAF50)
        isInCorrect -> Color(0xFFF44336)
        isSelected -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isCorrect -> Color.White
        isInCorrect -> Color.White
        isSelected -> MaterialTheme.colorScheme.onSecondary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    val borderColor = when {
        isSelected && !isCorrect && !isInCorrect -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 5.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            color = borderColor,
            width = 4.dp
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 6.dp else 2.dp
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(20.dp),
            color = textColor,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        )
    }
}

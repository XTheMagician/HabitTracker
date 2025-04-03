package com.example.habit_tracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.ui.graphics.vector.ImageVector

fun getIconForMood(mood: Mood): ImageVector = when (mood) {
    Mood.VERY_GOOD -> Icons.Filled.SentimentVerySatisfied
    Mood.GOOD -> Icons.Filled.SentimentSatisfied
    Mood.NEUTRAL -> Icons.Filled.SentimentNeutral
    Mood.BAD -> Icons.Filled.SentimentDissatisfied
    Mood.VERY_BAD -> Icons.Filled.SentimentVeryDissatisfied
}

fun getLabelForMood(mood: Mood): String = when (mood) {
    Mood.VERY_GOOD -> "Very Good"
    Mood.GOOD -> "Good"
    Mood.NEUTRAL -> "Okay"
    Mood.BAD -> "Bad"
    Mood.VERY_BAD -> "Very Bad"
}

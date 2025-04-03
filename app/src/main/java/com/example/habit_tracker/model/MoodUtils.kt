package com.example.habit_tracker.model

fun getEmojiForMood(mood: Mood): String = when (mood) {
    Mood.VERY_GOOD -> "😄"
    Mood.GOOD -> "🙂"
    Mood.NEUTRAL -> "😐"
    Mood.BAD -> "☹️"
    Mood.VERY_BAD -> "😣"
}

//Maybe language option later and Emoji set option
fun getLabelForMood(mood: Mood): String = when (mood) {
    Mood.VERY_GOOD -> "Super"
    Mood.GOOD -> "Gut"
    Mood.NEUTRAL -> "Ok"
    Mood.BAD -> "Schlecht"
    Mood.VERY_BAD -> "Lausig"
}

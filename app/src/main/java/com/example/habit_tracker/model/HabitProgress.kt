package com.example.habit_tracker.model

enum class ScaleLevel {
    NONE, LOW, MEDIUM, HIGH, EXTREME
}

data class HabitProgress(
    val habit: Habit,
    val binaryDone: Boolean? = null,
    val scale: ScaleLevel? = null
)

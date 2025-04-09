package com.example.habit_tracker.model

data class HabitProgress(
    val habit: Habit,
    val value: Int? = null
)
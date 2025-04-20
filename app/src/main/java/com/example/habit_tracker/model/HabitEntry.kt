package com.example.habit_tracker.model

import com.example.habit_tracker.data.db.HabitEntryEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

enum class Mood {
    VERY_GOOD, GOOD, NEUTRAL, BAD, VERY_BAD
}

data class HabitEntry(
    val date: LocalDate,
    val mood: Mood,
    val habits: List<HabitProgress>
)

fun HabitEntry.toEntity(): HabitEntryEntity {
    return HabitEntryEntity(
        date = this.date.format(DateTimeFormatter.ISO_DATE),
        mood = this.mood.name
    )
}

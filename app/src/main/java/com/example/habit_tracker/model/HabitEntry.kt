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
    // Holds the detailed progress for each habit in this entry
    val habits: List<HabitProgress> // Correctly holds HabitProgress list
)

// --- Corrected toEntity() function ---
fun HabitEntry.toEntity(): HabitEntryEntity {
    // Convert only the fields that exist in HabitEntryEntity
    return HabitEntryEntity(
        date = this.date.format(DateTimeFormatter.ISO_DATE), // Store date as ISO String
        mood = this.mood.name // Store mood enum as its String name
        // DO NOT include habitIds here, it was removed from HabitEntryEntity
    )
}

// --- Helper functions for Mood (Assuming they exist here or elsewhere) ---

// You'll need functions to convert Mood enum to/from the stored String if needed,
// and potentially functions to get icons/labels if they are defined here.
// Example placeholders:

// fun getMoodFromString(moodName: String?): Mood {
//    return Mood.values().firstOrNull { it.name == moodName } ?: Mood.NEUTRAL
// }

// fun getIconForMood(mood: Mood): ImageVector { ... }
// fun getLabelForMood(mood: Mood): String { ... }

// --- (Keep HabitProgress data class definition if it's in this file) ---

package com.example.habit_tracker.data

import androidx.compose.runtime.mutableStateListOf
import com.example.habit_tracker.model.HabitEntry
import java.time.LocalDate

object EntryRepository {

    private val entries = mutableStateListOf<HabitEntry>()

    fun addEntry(entry: HabitEntry) {
        entries.removeAll { it.date == entry.date }
        entries.add(0, entry) // add newest on top
    }

    fun getEntries(): List<HabitEntry> {
        return entries
    }

    fun clear() {
        entries.clear()
    }

    fun removeEntry(date: LocalDate) {
        entries.removeAll { it.date == date }
    }

}

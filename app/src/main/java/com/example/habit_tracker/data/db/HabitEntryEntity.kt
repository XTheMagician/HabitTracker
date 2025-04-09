package com.example.habit_tracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_entries")
data class HabitEntryEntity(
    @PrimaryKey val date: String,
    val mood: String
)
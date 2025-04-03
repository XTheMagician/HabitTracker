package com.example.habit_tracker.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.habit_tracker.model.HabitType

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val iconName: String,
    val type: HabitType = HabitType.BINARY,
    val category: String? = null
)

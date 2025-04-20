package com.example.habit_tracker.model

import com.example.habit_tracker.data.db.HabitEntity

enum class HabitType {
    BINARY,
    SCALE
}

data class Habit(
    val id: Int,
    val name: String,
    val iconKey: String,
    val type: HabitType = HabitType.BINARY,
    val category: String = "General"
)

fun HabitEntity.toUiModel(): Habit {
    return Habit(
        id = id,
        name = name,
        iconKey = iconName,
        type = type,
        category = category ?: "General"
    )
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = this.id,
        name = this.name,
        iconName = this.iconKey,
        type = this.type,
        category = this.category
    )
}
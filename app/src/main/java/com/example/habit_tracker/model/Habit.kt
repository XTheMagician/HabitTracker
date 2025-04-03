package com.example.habit_tracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.habit_tracker.data.db.HabitEntity
import iconMap

enum class HabitType {
    BINARY,
    SCALE
}

data class Habit(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val iconKey: String,
    val type: HabitType = HabitType.BINARY,
    val category: String = "General"
)


fun HabitEntity.toUiModel(): Habit {
    return Habit(
        id = id,
        name = name,
        icon = getIconByName(iconName),
        iconKey = iconName,
        type = type,
        category = category ?: "General"
    )
}


fun getIconByName(name: String): ImageVector {
    return iconMap[name] ?: Icons.Filled.Check
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

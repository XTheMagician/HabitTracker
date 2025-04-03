package com.example.habit_tracker.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.habit_tracker.data.db.HabitEntity

enum class HabitType {
    BINARY,
    SCALE
}

data class Habit(
    val id: Int,
    val name: String,
    val icon: ImageVector,
    val type: HabitType = HabitType.BINARY,
    val category: String = "General"
)


fun HabitEntity.toUiModel(): Habit {
    return Habit(
        id = id,
        name = name,
        icon = getIconByName(iconName),
        type = type,
        category = category ?: "General"
    )
}

fun getIconByName(name: String): ImageVector {
    return when (name) {
        "MenuBook" -> Icons.AutoMirrored.Filled.MenuBook
        "FitnessCenter" -> Icons.Default.FitnessCenter
        "School" -> Icons.Default.School
        "SelfImprovement" -> Icons.Default.SelfImprovement
        "Bedtime" -> Icons.Default.Bedtime
        else -> Icons.Default.Check
    }
}

fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = this.id,
        name = this.name,
        iconName = this.icon.name,
        type = this.type,
        category = this.category
    )
}
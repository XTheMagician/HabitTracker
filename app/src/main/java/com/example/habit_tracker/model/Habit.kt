package com.example.habit_tracker.model

// Remove ImageVector import if no longer needed anywhere else in this file
// import androidx.compose.ui.graphics.vector.ImageVector
import com.example.habit_tracker.data.db.HabitEntity

// REMOVE import for AppIcons as it should be deleted
// import com.example.habit_tracker.ui.theme.AppIcons

enum class HabitType {
    BINARY,
    SCALE
}

// *** MODIFIED Habit data class ***
data class Habit(
    val id: Int,
    val name: String,
    // val icon: ImageVector, // REMOVED - We only need the name now
    val iconKey: String, // This now holds the Material Symbol Name
    val type: HabitType = HabitType.BINARY,
    val category: String = "General"
)

// *** MODIFIED toUiModel function ***
fun HabitEntity.toUiModel(): Habit {
    return Habit(
        id = id,
        name = name,
        // icon = getIconByName(iconName), // REMOVED this line
        iconKey = iconName, // Assign the name directly from the entity
        type = type,
        category = category ?: "General"
    )
}

// *** REMOVE the getIconByName function entirely ***
/* // DELETE this function - it uses the old system
fun getIconByName(name: String): ImageVector {
    return AppIcons.map[name] ?: AppIcons.defaultIcon
}
*/

// toEntity function remains correct as it uses iconKey
fun Habit.toEntity(): HabitEntity {
    return HabitEntity(
        id = this.id,
        name = this.name,
        iconName = this.iconKey, // Uses the symbol name from Habit
        type = this.type,
        category = this.category
    )
}
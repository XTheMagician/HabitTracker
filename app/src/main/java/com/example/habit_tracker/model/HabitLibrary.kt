import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.habit_tracker.model.Habit
import com.example.habit_tracker.model.HabitType
import androidx.compose.material.icons.automirrored.filled.*

object HabitLibrary {
    val habits = listOf(
        Habit(1, "Read Book", Icons.AutoMirrored.Filled.MenuBook, HabitType.BINARY),
        Habit(2, "Workout", Icons.Default.FitnessCenter, HabitType.BINARY),
        Habit(3, "Study", Icons.Default.School, HabitType.SCALE),
        Habit(4, "Meditation", Icons.Default.SelfImprovement, HabitType.BINARY),
        Habit(5, "Sleep Early", Icons.Default.Bedtime, HabitType.BINARY)
    )

    fun getByName(name: String): Habit? =
        habits.find { it.name == name }

    fun getById(id: Int): Habit? =
        habits.find { it.id == id }
}
package com.example.habit_tracker.ui.navigation

import android.net.Uri
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habit_tracker.model.HabitType
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.ui.screens.habit.AddHabitDetailsScreen
import com.example.habit_tracker.ui.screens.habit.HabitSelectionScreen
import com.example.habit_tracker.ui.screens.habit.SelectHabitIconScreen
import com.example.habit_tracker.ui.screens.home.HomeScreen
import com.example.habit_tracker.ui.screens.mood.AddEntryScreen
import com.example.habit_tracker.ui.screens.stats.StatisticsScreen
import com.example.habit_tracker.viewmodel.HabitEntryViewModel
import com.example.habit_tracker.viewmodel.HabitViewModel
import java.time.LocalDate

object AppDestinations {
    const val HOME = "home"
    const val ADD_ENTRY = "addEntry"
    const val HABIT_SELECTION = "habitSelection"
    const val ADD_HABIT_DETAILS = "add_habit_details"
    const val SELECT_HABIT_ICON = "select_habit_icon"
    const val STATISTICS = "statistics"

    const val HABIT_SELECTION_ROUTE = "$HABIT_SELECTION/{mood}/{date}"
    const val ADD_HABIT_DETAILS_ROUTE = "$ADD_HABIT_DETAILS/{category}"
    const val SELECT_HABIT_ICON_ROUTE = "$SELECT_HABIT_ICON/{category}/{name}/{type}"

    fun buildHabitSelectionRoute(mood: Mood, date: LocalDate): String =
        "$HABIT_SELECTION/${mood.name}/${date.toString()}"

    fun buildAddHabitDetailsRoute(category: String): String =
        "$ADD_HABIT_DETAILS/${Uri.encode(category)}"

    fun buildSelectHabitIconRoute(category: String, name: String, type: HabitType): String =
        "$SELECT_HABIT_ICON/${Uri.encode(category)}/${Uri.encode(name)}/${type.name}"
}

@Composable
fun AppNavigation() {
    val navController: NavHostController = rememberNavController()

    NavHost(navController = navController, startDestination = AppDestinations.HOME) {

        composable(route = AppDestinations.HOME) {
            HomeScreen(navController)
        }

        composable(route = AppDestinations.ADD_ENTRY) {
            AddEntryScreen(navController)
        }

        composable(route = AppDestinations.STATISTICS) { // <-- ADD THIS BLOCK
            StatisticsScreen(navController = navController) // Pass NavController
        }

        composable(
            route = AppDestinations.HABIT_SELECTION_ROUTE,
            arguments = listOf(
                navArgument("mood") { type = NavType.StringType },
                navArgument("date") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val moodString = backStackEntry.arguments?.getString("mood")
            val dateString = backStackEntry.arguments?.getString("date")
            val mood = moodString?.let { Mood.valueOf(it) }
            val date = dateString?.let { LocalDate.parse(it) }

            if (mood != null && date != null) {
                val habitViewModel: HabitViewModel = viewModel()
                val entryViewModel: HabitEntryViewModel = viewModel()

                HabitSelectionScreen(
                    navController = navController,
                    mood = mood,
                    date = date,
                    entryViewModel = entryViewModel,
                    habitViewModel = habitViewModel
                )
            } else {
                Text("Error: Missing mood or date information.")
            }
        }

        composable(
            route = AppDestinations.ADD_HABIT_DETAILS_ROUTE,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = Uri.decode(backStackEntry.arguments?.getString("category"))
                ?: "Unknown Category"

            AddHabitDetailsScreen(
                navController = navController,
                category = category
            )
        }

        composable(
            route = AppDestinations.SELECT_HABIT_ICON_ROUTE,
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("name") { type = NavType.StringType },
                navArgument("type") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val category = Uri.decode(backStackEntry.arguments?.getString("category")) ?: ""
            val name = Uri.decode(backStackEntry.arguments?.getString("name")) ?: ""
            val typeString = backStackEntry.arguments?.getString("type")
            val type = HabitType.values().firstOrNull { it.name == typeString }
                ?: HabitType.BINARY

            val habitViewModel: HabitViewModel = viewModel()

            SelectHabitIconScreen(
                navController = navController,
                habitViewModel = habitViewModel,
                category = category,
                habitName = name,
                habitType = type
            )
        }
    }
}
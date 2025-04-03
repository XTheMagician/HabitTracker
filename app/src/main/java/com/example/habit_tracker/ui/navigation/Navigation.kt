package com.example.habit_tracker.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.ui.screens.habit.HabitSelectionScreen
import com.example.habit_tracker.ui.screens.home.HomeScreen
import com.example.habit_tracker.ui.screens.mood.AddEntryScreen
import java.time.LocalDate

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(navController)
        }
        composable("addEntry") {
            AddEntryScreen(navController)
        }
        composable(
            "habitSelection/{mood}/{date}",
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
                HabitSelectionScreen(navController, mood, date)
            } else {
                Text("Missing mood or date")
            }
        }

    }
}

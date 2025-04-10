package com.example.habit_tracker.ui.screens.stats // Adjust package if needed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.ui.components.BottomNavigationBar
import com.example.habit_tracker.ui.components.HabitFrequencyCard
import com.example.habit_tracker.ui.components.MoodChartCard
import com.example.habit_tracker.ui.components.MoodLineChartCard
import com.example.habit_tracker.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = viewModel() // Uses the single ViewModel instance
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        // Column is already set up to be scrollable
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Horizontal padding for the content
                .verticalScroll(rememberScrollState()) // Enables scrolling
        ) {
            // Card 1: Mood Column Chart
            MoodChartCard(viewModel = statisticsViewModel)
            // Optional Spacer if you want space back

            // Card 2: Mood Line Chart
            MoodLineChartCard(viewModel = statisticsViewModel)
            // Optional Spacer

            // --- Card 3: Habit Frequency List (NEW) ---
            HabitFrequencyCard(viewModel = statisticsViewModel)
            // --- End New Card ---


            // Optional padding at the very bottom inside the scrollable area
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
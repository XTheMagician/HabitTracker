package com.example.habit_tracker.ui.screens.stats // Adjust package if needed

// --- Import MonthSwitcher ---
// --- Import Date/Time related items ---
// --- ViewModel ---
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.ui.components.BottomNavigationBar
import com.example.habit_tracker.ui.components.HabitFrequencyCard
import com.example.habit_tracker.ui.components.MoodChartCard
import com.example.habit_tracker.ui.components.MoodLineChartCard
import com.example.habit_tracker.ui.screens.home.MonthSwitcher
import com.example.habit_tracker.viewmodel.StatisticsViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = viewModel() // Uses the single ViewModel instance
) {
    // --- Collect currentYearMonth state from ViewModel ---
    val currentYearMonth by statisticsViewModel.currentYearMonth.collectAsStateWithLifecycle()

    // --- Create the formatter (same as HomeScreen) ---
    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics") })
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Apply horizontal padding once
                .verticalScroll(rememberScrollState())
        ) {
            // --- Add the MonthSwitcher ---
            MonthSwitcher(
                currentMonth = currentYearMonth,
                monthYearFormatter = monthYearFormatter,
                onPreviousMonth = statisticsViewModel::showPreviousMonth, // Use method reference
                onNextMonth = statisticsViewModel::showNextMonth,     // Use method reference
                modifier = Modifier.padding(vertical = 8.dp) // Add some vertical padding
            )
            // --- End MonthSwitcher ---

            Spacer(modifier = Modifier.height(8.dp)) // Add space below switcher

            // Card 1: Mood Column Chart
            MoodChartCard(viewModel = statisticsViewModel)

            // Card 2: Mood Line Chart
            MoodLineChartCard(viewModel = statisticsViewModel)

            // Card 3: Habit Frequency List
            HabitFrequencyCard(viewModel = statisticsViewModel)

            // Optional padding at the very bottom
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
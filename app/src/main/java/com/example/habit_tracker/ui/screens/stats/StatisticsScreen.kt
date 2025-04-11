package com.example.habit_tracker.ui.screens.stats // Adjust package if needed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.ui.components.BottomNavigationBar
import com.example.habit_tracker.ui.components.HabitFrequencyCard
import com.example.habit_tracker.ui.components.MoodChartCard
import com.example.habit_tracker.ui.components.MoodLineChartCard
import com.example.habit_tracker.ui.components.YearSwitcher
import com.example.habit_tracker.ui.screens.home.MonthSwitcher
import com.example.habit_tracker.viewmodel.StatisticsMode
import com.example.habit_tracker.viewmodel.StatisticsViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController,
    statisticsViewModel: StatisticsViewModel = viewModel()
) {
    // --- Collect State from ViewModel ---
    val currentMode by statisticsViewModel.statisticsMode.collectAsStateWithLifecycle()
    val currentYearMonth by statisticsViewModel.currentYearMonth.collectAsStateWithLifecycle()
    val currentYear by statisticsViewModel.currentYear.collectAsStateWithLifecycle()
    // Collect showMoodChart flag to conditionally display charts
    val showMoodCharts by statisticsViewModel.showMoodChart.collectAsStateWithLifecycle()
    // Collect mood summary data (optional, for display if needed)
    val moodSummary by statisticsViewModel.moodSummaryData.collectAsStateWithLifecycle()

    // --- Formatter for MonthSwitcher ---
    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() // Use full size for the main column
        ) {
            // --- Mode Selector Tabs ---
            StatisticsModeTabs(
                selectedMode = currentMode,
                onModeSelected = { statisticsViewModel.setMode(it) } // Call VM function on tab click
            )

            // --- Scrollable Content Area ---
            Column(
                modifier = Modifier
                    .weight(1f) // Allow this column to take remaining space
                    .verticalScroll(rememberScrollState()) // Make this inner column scrollable
                    .padding(horizontal = 16.dp) // Apply padding to the content area
            ) {
                Spacer(modifier = Modifier.height(8.dp)) // Space below tabs

                // --- Conditional Time Period Switcher ---
                when (currentMode) {
                    StatisticsMode.MONTHLY -> {
                        MonthSwitcher(
                            currentMonth = currentYearMonth,
                            monthYearFormatter = monthYearFormatter,
                            // Use the correct ViewModel functions
                            onPreviousMonth = statisticsViewModel::showPreviousTimePeriod,
                            onNextMonth = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    StatisticsMode.YEARLY -> {
                        YearSwitcher(
                            currentYear = currentYear,
                            // Use the correct ViewModel functions
                            onPreviousYear = statisticsViewModel::showPreviousTimePeriod,
                            onNextYear = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp)) // Space below switcher

                // --- Statistics Cards ---

                // Conditionally display Mood Charts only if in Monthly mode AND flag is true
                if (currentMode == StatisticsMode.MONTHLY && showMoodCharts) {
                    MoodChartCard(viewModel = statisticsViewModel)
                    MoodLineChartCard(viewModel = statisticsViewModel)
                    Spacer(modifier = Modifier.height(8.dp)) // Add space after charts if they are shown
                }

                // Optional: Display Mood Summary (useful for Yearly view)
                // You might want to create a dedicated MoodSummaryCard composable
                if (moodSummary != null && currentMode == StatisticsMode.YEARLY) { // Example: Show only in yearly
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "Yearly Mood Summary",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                            Text("Average Score: ${moodSummary?.averageScore?.let { "%.1f".format(it) } ?: "N/A"}")
                            Spacer(Modifier.height(4.dp))
                            Text("Distribution:")
                            moodSummary?.distribution?.forEach { (mood, count) ->
                                Text("- ${mood.name}: $count")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }


                // Habit Frequency Card - Reusable for both modes
                HabitFrequencyCard(viewModel = statisticsViewModel)

                // Add future statistic cards here...

                Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
            }
        }
    }
}


// --- Separate Composable for Mode Tabs ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsModeTabs( // Make private if only used here
    selectedMode: StatisticsMode,
    onModeSelected: (StatisticsMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = StatisticsMode.values()
    // Use PrimaryTabRow for Material 3 styling
    PrimaryTabRow(
        selectedTabIndex = selectedMode.ordinal, // Use enum ordinal for index
        modifier = modifier.fillMaxWidth() // Take full width
    ) {
        modes.forEachIndexed { index, mode ->
            Tab(
                selected = selectedMode == mode, // Check for equality
                onClick = { onModeSelected(mode) }, // Trigger callback
                text = {
                    Text(
                        // Capitalize the mode name for display
                        text = mode.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        maxLines = 1, // Prevent wrapping
                        overflow = TextOverflow.Ellipsis // Handle long text if necessary
                    )
                }
            )
        }
    }
}
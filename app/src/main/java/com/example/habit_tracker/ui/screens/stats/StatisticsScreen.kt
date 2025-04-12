package com.example.habit_tracker.ui.screens.stats // Adjust package if needed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.habit_tracker.ui.components.HabitYearInPixelsCard
import com.example.habit_tracker.ui.components.MoodChartCard
import com.example.habit_tracker.ui.components.MoodDistributionChartCard
import com.example.habit_tracker.ui.components.MoodLineChartCard
import com.example.habit_tracker.ui.components.YearInPixelsCard
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
    val showMoodCharts by statisticsViewModel.showMoodChart.collectAsStateWithLifecycle()
    val moodSummary by statisticsViewModel.moodSummaryData.collectAsStateWithLifecycle()
    val isMoodLoading by statisticsViewModel.isMoodLoading.collectAsStateWithLifecycle()
    val yearPixelsData by statisticsViewModel.yearInPixelsData.collectAsStateWithLifecycle()
    val isYearPixelsLoading by statisticsViewModel.isYearInPixelsLoading.collectAsStateWithLifecycle()
    // *** Collect Habit Pixel State ***
    val allHabits by statisticsViewModel.allHabits.collectAsStateWithLifecycle()
    val selectedHabitIdForPixels by statisticsViewModel.selectedHabitIdForPixels.collectAsStateWithLifecycle()
    val habitPixelData by statisticsViewModel.habitPixelData.collectAsStateWithLifecycle()
    val isHabitPixelLoading by statisticsViewModel.isHabitPixelLoading.collectAsStateWithLifecycle()
    // *** End Collect ***

    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            StatisticsModeTabs(
                selectedMode = currentMode,
                onModeSelected = { statisticsViewModel.setMode(it) }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // --- Conditional Time Period Switcher ---
                when (currentMode) {
                    StatisticsMode.MONTHLY -> {
                        MonthSwitcher( // ... parameters ...
                            currentMonth = currentYearMonth,
                            monthYearFormatter = monthYearFormatter,
                            onPreviousMonth = statisticsViewModel::showPreviousTimePeriod,
                            onNextMonth = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    StatisticsMode.YEARLY -> {
                        YearSwitcher( // ... parameters ...
                            currentYear = currentYear,
                            onPreviousYear = statisticsViewModel::showPreviousTimePeriod,
                            onNextYear = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))

                // --- Statistics Cards ---

                // Monthly Mood Charts
                if (currentMode == StatisticsMode.MONTHLY && showMoodCharts) {
                    MoodChartCard(viewModel = statisticsViewModel)
                    MoodLineChartCard(viewModel = statisticsViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Yearly Cards
                if (currentMode == StatisticsMode.YEARLY) {
                    // Yearly Mood Distribution Chart
                    MoodDistributionChartCard(
                        title = "Yearly Mood Distribution",
                        moodDistribution = moodSummary?.distribution,
                        isLoading = isMoodLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Mood Year In Pixels Card
                    YearInPixelsCard(
                        selectedYear = currentYear,
                        pixelData = yearPixelsData,
                        isLoading = isYearPixelsLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // *** Add Habit Year In Pixels Card ***
                    HabitYearInPixelsCard(
                        selectedYear = currentYear,
                        allHabits = allHabits, // Pass list of habits
                        selectedHabitId = selectedHabitIdForPixels, // Pass selected ID
                        habitPixelData = habitPixelData, // Pass habit pixel map
                        isLoading = isHabitPixelLoading, // Pass loading state
                        onHabitSelected = statisticsViewModel::selectHabitForPixels // Pass callback
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // *** End Habit Year In Pixels Card ***
                }

                // Habit Frequency Card (Works for both modes)
                HabitFrequencyCard(viewModel = statisticsViewModel)

                Spacer(modifier = Modifier.height(16.dp)) // Bottom padding
            }
        }
    }
}


// --- StatisticsModeTabs Composable (private) ---
@OptIn(ExperimentalMaterial3Api::class) // Needed for PrimaryTabRow/Tab
@Composable
private fun StatisticsModeTabs(
    selectedMode: StatisticsMode,
    onModeSelected: (StatisticsMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = StatisticsMode.values() // Get all enum values (MONTHLY, YEARLY)
    PrimaryTabRow(
        selectedTabIndex = selectedMode.ordinal, // Use enum ordinal (0 for MONTHLY, 1 for YEARLY)
        modifier = modifier.fillMaxWidth() // Make the row take full width
    ) {
        // Create a Tab for each mode in the enum
        modes.forEachIndexed { index, mode ->
            Tab(
                selected = selectedMode == mode, // Highlight the tab if it's the selected mode
                onClick = { onModeSelected(mode) }, // Call the provided lambda when clicked
                text = {
                    Text(
                        // Display the mode name, capitalized
                        text = mode.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        maxLines = 1, // Prevent text wrapping
                        overflow = TextOverflow.Ellipsis // Handle cases if text is too long
                    )
                }
                // You can also add icons here if desired using the 'icon = { ... }' parameter
            )
        }
    }
}
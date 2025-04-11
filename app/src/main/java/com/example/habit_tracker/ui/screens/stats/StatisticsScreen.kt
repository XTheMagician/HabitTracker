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

    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }

    Scaffold(
        // Removed TopAppBar
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // --- Mode Selector Tabs ---
            StatisticsModeTabs(
                selectedMode = currentMode,
                onModeSelected = { statisticsViewModel.setMode(it) }
            )

            // --- Scrollable Content Area ---
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
                        MonthSwitcher(
                            currentMonth = currentYearMonth,
                            monthYearFormatter = monthYearFormatter,
                            onPreviousMonth = statisticsViewModel::showPreviousTimePeriod,
                            onNextMonth = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }

                    StatisticsMode.YEARLY -> {
                        YearSwitcher(
                            currentYear = currentYear,
                            onPreviousYear = statisticsViewModel::showPreviousTimePeriod,
                            onNextYear = statisticsViewModel::showNextTimePeriod,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // --- Statistics Cards ---

                // Conditionally display Mood Line/Column Charts only in Monthly mode
                if (currentMode == StatisticsMode.MONTHLY && showMoodCharts) {
                    MoodChartCard(viewModel = statisticsViewModel)
                    MoodLineChartCard(viewModel = statisticsViewModel)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // *** Use MoodDistributionChartCard in Yearly mode ***
                if (currentMode == StatisticsMode.YEARLY) {
                    MoodDistributionChartCard(
                        title = "Yearly Mood Distribution",
                        // Pass the distribution map from the summary object
                        moodDistribution = moodSummary?.distribution,
                        isLoading = isMoodLoading // Use the mood loading flag
                    )
                    YearInPixelsCard(
                        selectedYear = currentYear, // Pass the selected year
                        pixelData = yearPixelsData, // Pass the map data
                        isLoading = isYearPixelsLoading // Pass the loading state
                    )
                }
                HabitFrequencyCard(viewModel = statisticsViewModel)
                Spacer(modifier = Modifier.height(8.dp)) // Bottom padding
            }
        }
    }
}


// --- StatisticsModeTabs Composable (private) ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsModeTabs(
    selectedMode: StatisticsMode,
    onModeSelected: (StatisticsMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = StatisticsMode.values()
    PrimaryTabRow(
        selectedTabIndex = selectedMode.ordinal,
        modifier = modifier.fillMaxWidth()
    ) {
        modes.forEachIndexed { index, mode ->
            Tab(
                selected = selectedMode == mode,
                onClick = { onModeSelected(mode) },
                text = {
                    Text(
                        text = mode.name.lowercase()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            )
        }
    }
}
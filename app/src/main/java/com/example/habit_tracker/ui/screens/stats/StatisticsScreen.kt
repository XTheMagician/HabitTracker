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
import com.example.habit_tracker.ui.components.HabitMoodCorrelationListCard
import com.example.habit_tracker.ui.components.HabitYearInPixelsCard
// Import MoodChartCard only if still used elsewhere, otherwise remove
// import com.example.habit_tracker.ui.components.MoodChartCard
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
    val currentMode by statisticsViewModel.statisticsMode.collectAsStateWithLifecycle()
    val currentYearMonth by statisticsViewModel.currentYearMonth.collectAsStateWithLifecycle()
    val currentYear by statisticsViewModel.currentYear.collectAsStateWithLifecycle()
    // val showMoodCharts by statisticsViewModel.showMoodChart.collectAsStateWithLifecycle() // Removed if not needed
    val moodSummary by statisticsViewModel.moodSummaryData.collectAsStateWithLifecycle()
    val isMoodLoading by statisticsViewModel.isMoodLoading.collectAsStateWithLifecycle()
    val yearPixelsData by statisticsViewModel.yearInPixelsData.collectAsStateWithLifecycle()
    val isYearPixelsLoading by statisticsViewModel.isYearInPixelsLoading.collectAsStateWithLifecycle()
    val allHabits by statisticsViewModel.allHabits.collectAsStateWithLifecycle()
    val selectedHabitIdForPixels by statisticsViewModel.selectedHabitIdForPixels.collectAsStateWithLifecycle()
    val habitPixelData by statisticsViewModel.habitPixelData.collectAsStateWithLifecycle()
    val isHabitPixelLoading by statisticsViewModel.isHabitPixelLoading.collectAsStateWithLifecycle()
    val allCorrelationResults by statisticsViewModel.allCorrelationResults.collectAsStateWithLifecycle()
    val isAllCorrelationsLoading by statisticsViewModel.isAllCorrelationsLoading.collectAsStateWithLifecycle()

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

                if (currentMode == StatisticsMode.MONTHLY) {
                    MoodDistributionChartCard(
                        title = "Mood Distribution for ${monthYearFormatter.format(currentYearMonth)}",
                        moodDistribution = moodSummary?.distribution,
                        isLoading = isMoodLoading
                    )
                    MoodLineChartCard(viewModel = statisticsViewModel)
                }

                if (currentMode == StatisticsMode.YEARLY) {
                    MoodDistributionChartCard(
                        title = "Yearly Mood Distribution",
                        moodDistribution = moodSummary?.distribution,
                        isLoading = isMoodLoading
                    )
                    YearInPixelsCard(
                        selectedYear = currentYear,
                        pixelData = yearPixelsData,
                        isLoading = isYearPixelsLoading
                    )
                    HabitYearInPixelsCard(
                        selectedYear = currentYear,
                        allHabits = allHabits,
                        selectedHabitId = selectedHabitIdForPixels,
                        habitPixelData = habitPixelData,
                        isLoading = isHabitPixelLoading,
                        onHabitSelected = statisticsViewModel::selectHabitForPixels
                    )
                }

                HabitFrequencyCard(viewModel = statisticsViewModel)
                HabitMoodCorrelationListCard(
                    viewModel = statisticsViewModel
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

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
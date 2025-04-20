package com.example.habit_tracker.ui.screens.stats

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

            StatisticsContent(
                viewModel = statisticsViewModel,
                currentMode = currentMode
            )
        }
    }
}

@Composable
private fun StatisticsContent(
    viewModel: StatisticsViewModel,
    currentMode: StatisticsMode,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = modifier
            .weight(1f)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        
        TimePeriodSelector(viewModel = viewModel, currentMode = currentMode)
        
        when (currentMode) {
            StatisticsMode.MONTHLY -> MonthlyStatistics(viewModel = viewModel)
            StatisticsMode.YEARLY -> YearlyStatistics(viewModel = viewModel)
        }
        
        CommonStatistics(viewModel = viewModel)
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun TimePeriodSelector(
    viewModel: StatisticsViewModel,
    currentMode: StatisticsMode
) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }
    
    when (currentMode) {
        StatisticsMode.MONTHLY -> {
            MonthSwitcher(
                currentMonth = currentYearMonth,
                monthYearFormatter = monthYearFormatter,
                onPreviousMonth = viewModel::showPreviousTimePeriod,
                onNextMonth = viewModel::showNextTimePeriod,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        StatisticsMode.YEARLY -> {
            YearSwitcher(
                currentYear = currentYear,
                onPreviousYear = viewModel::showPreviousTimePeriod,
                onNextYear = viewModel::showNextTimePeriod,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}

@Composable
private fun MonthlyStatistics(viewModel: StatisticsViewModel) {
    val currentYearMonth by viewModel.currentYearMonth.collectAsStateWithLifecycle()
    val moodSummary by viewModel.moodSummaryData.collectAsStateWithLifecycle()
    val isMoodLoading by viewModel.isMoodLoading.collectAsStateWithLifecycle()
    
    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }
    
    MoodDistributionChartCard(
        title = "Mood Distribution for ${monthYearFormatter.format(currentYearMonth)}",
        moodDistribution = moodSummary?.distribution,
        isLoading = isMoodLoading
    )
    
    MoodLineChartCard(viewModel = viewModel)
}

@Composable
private fun YearlyStatistics(viewModel: StatisticsViewModel) {
    val currentYear by viewModel.currentYear.collectAsStateWithLifecycle()
    val moodSummary by viewModel.moodSummaryData.collectAsStateWithLifecycle()
    val isMoodLoading by viewModel.isMoodLoading.collectAsStateWithLifecycle()
    val yearPixelsData by viewModel.yearInPixelsData.collectAsStateWithLifecycle()
    val isYearPixelsLoading by viewModel.isYearInPixelsLoading.collectAsStateWithLifecycle()
    val allHabits by viewModel.allHabits.collectAsStateWithLifecycle()
    val selectedHabitIdForPixels by viewModel.selectedHabitIdForPixels.collectAsStateWithLifecycle()
    val habitPixelData by viewModel.habitPixelData.collectAsStateWithLifecycle()
    val isHabitPixelLoading by viewModel.isHabitPixelLoading.collectAsStateWithLifecycle()
    
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
        onHabitSelected = viewModel::selectHabitForPixels
    )
}

@Composable
private fun CommonStatistics(viewModel: StatisticsViewModel) {
    HabitFrequencyCard(viewModel = viewModel)
    HabitMoodCorrelationListCard(viewModel = viewModel)
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
        modes.forEachIndexed { _, mode ->
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
package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitProgressDao
import com.example.habit_tracker.data.db.HabitProgressEntity
import com.example.habit_tracker.data.db.MoodDataPoint
import com.example.habit_tracker.model.Mood
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Enum for Mode
enum class StatisticsMode {
    MONTHLY, YEARLY
}

// Data class for Habit Frequency result
data class HabitFrequencyStat(
    val name: String,
    val count: Int,
    val iconName: String
)

// Data class for Mood Summary result
data class MoodSummary(
    val averageScore: Float?,
    val distribution: Map<Mood, Int>
)

@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    // --- DAOs ---
    private val entryDao: HabitEntryDao = AppDatabase.getDatabase(application).habitEntryDao()
    private val habitDao: HabitDao = AppDatabase.getDatabase(application).habitDao()
    private val progressDao: HabitProgressDao =
        AppDatabase.getDatabase(application).habitProgressDao()

    // --- State for Mode and Time Range ---
    private val _statisticsMode = MutableStateFlow(StatisticsMode.MONTHLY)
    val statisticsMode: StateFlow<StatisticsMode> = _statisticsMode.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    private val _currentYear = MutableStateFlow(Year.now())
    val currentYear: StateFlow<Year> = _currentYear.asStateFlow()

    // --- Mood Data State ---
    private val _moodChartData = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val moodChartData: StateFlow<List<Pair<Float, Float>>> = _moodChartData.asStateFlow()

    private val _showMoodChart = MutableStateFlow(false)
    val showMoodChart: StateFlow<Boolean> = _showMoodChart.asStateFlow()

    private val _isMoodLoading = MutableStateFlow(true)
    val isMoodLoading: StateFlow<Boolean> = _isMoodLoading.asStateFlow()

    private val _moodSummaryData = MutableStateFlow<MoodSummary?>(null)
    val moodSummaryData: StateFlow<MoodSummary?> = _moodSummaryData.asStateFlow()

    // --- Habit Frequency State ---
    private val _habitFrequencyData = MutableStateFlow<List<HabitFrequencyStat>>(emptyList())
    val habitFrequencyData: StateFlow<List<HabitFrequencyStat>> = _habitFrequencyData.asStateFlow()

    private val _isHabitFrequencyLoading = MutableStateFlow(true)
    val isHabitFrequencyLoading: StateFlow<Boolean> = _isHabitFrequencyLoading.asStateFlow()

    private val _showHabitFrequency = MutableStateFlow(false)
    val showHabitFrequency: StateFlow<Boolean> = _showHabitFrequency.asStateFlow()

    // --- Mappings & Formatters ---
    private val moodToValueMap = mapOf(
        Mood.VERY_BAD to 0f,
        Mood.BAD to 1f,
        Mood.NEUTRAL to 2f,
        Mood.GOOD to 3f,
        Mood.VERY_GOOD to 4f
    )
    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

    // --- Initialization ---
    init {
        observeTimeRangeChangesAndLoadData()
    }

    // --- Public Control Functions ---
    fun setMode(mode: StatisticsMode) {
        _statisticsMode.value = mode
    }

    fun showPreviousTimePeriod() {
        when (_statisticsMode.value) {
            StatisticsMode.MONTHLY -> _currentYearMonth.value =
                _currentYearMonth.value.minusMonths(1)

            StatisticsMode.YEARLY -> _currentYear.value = _currentYear.value.minusYears(1)
        }
    }

    fun showNextTimePeriod() {
        when (_statisticsMode.value) {
            StatisticsMode.MONTHLY -> _currentYearMonth.value =
                _currentYearMonth.value.plusMonths(1)

            StatisticsMode.YEARLY -> _currentYear.value = _currentYear.value.plusYears(1)
        }
    }

    // --- Reactive Data Loading Trigger ---
    private fun observeTimeRangeChangesAndLoadData() {
        viewModelScope.launch {
            combine(statisticsMode, currentYearMonth, currentYear) { mode, month, year ->
                Triple(mode, month, year)
            }.distinctUntilChanged()
                .collectLatest { (mode, month, year) ->
                    Log.d(
                        "StatsVM",
                        "Reloading data for Mode: $mode, Time: ${if (mode == StatisticsMode.MONTHLY) month else year}"
                    )
                    // Calculate date range based on mode
                    val (startDate, endDate) = calculateDateRange(mode, month, year)
                    // Trigger specific loading functions with calculated dates
                    loadMoodData(mode, startDate, endDate)
                    loadHabitFrequencyData(startDate, endDate)
                }
        }
    }

    // --- Date Range Calculation ---
    private fun calculateDateRange(
        mode: StatisticsMode,
        month: YearMonth,
        year: Year
    ): Pair<LocalDate, LocalDate> {
        return when (mode) {
            StatisticsMode.MONTHLY -> Pair(month.atDay(1), month.atEndOfMonth())
            StatisticsMode.YEARLY -> Pair(year.atDay(1), year.atMonth(12).atEndOfMonth())
        }
    }

    // --- Private Data Loading & Processing Functions ---

    // Loads and processes mood data for the given date range
    private fun loadMoodData(mode: StatisticsMode, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isMoodLoading.value = true
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            // Use the single refactored DAO function
            entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                .catch { e ->
                    Log.e("StatsVM", "Error loading mood entries for $startDate - $endDate", e)
                    _moodChartData.value = emptyList()
                    _moodSummaryData.value = null
                    _showMoodChart.value = false
                    _isMoodLoading.value = false
                }
                .collectLatest { moodEntries ->
                    // Process summary always
                    _moodSummaryData.value = processMoodSummary(moodEntries)

                    // Handle charts based on mode
                    if (mode == StatisticsMode.MONTHLY) {
                        val hasEnoughData = moodEntries.size >= 2
                        _showMoodChart.value = hasEnoughData
                        _moodChartData.value =
                            if (hasEnoughData) processMoodChartData(moodEntries) else emptyList()
                    } else {
                        _showMoodChart.value = false
                        _moodChartData.value = emptyList()
                    }
                    _isMoodLoading.value = false
                }
        }
    }

    // Loads and processes habit frequency for the given date range
    private fun loadHabitFrequencyData(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isHabitFrequencyLoading.value = true
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            // Use the single refactored DAO function
            val progressFlow =
                progressDao.getAllProgressBetweenDates(startDateString, endDateString)

            combine(
                habitDao.getAllHabits(),
                progressFlow
            ) { habits, progressEntries ->
                processHabitFrequency(progressEntries, habits) // Call reusable processor
            }.catch { e ->
                Log.e(
                    "StatsVM",
                    "Error loading/processing habit frequency for $startDate - $endDate",
                    e
                )
                _habitFrequencyData.value = emptyList()
                _showHabitFrequency.value = false
                _isHabitFrequencyLoading.value = false
            }.collectLatest { processedFrequencyStatsList ->
                _habitFrequencyData.value = processedFrequencyStatsList
                _showHabitFrequency.value = processedFrequencyStatsList.isNotEmpty()
                _isHabitFrequencyLoading.value = false
            }
        }
    }

    // --- Private Reusable Processing Functions (Unchanged from previous version) ---

    private fun processMoodChartData(moodEntries: List<MoodDataPoint>): List<Pair<Float, Float>> {
        return moodEntries.mapIndexedNotNull { index, dataPoint ->
            try {
                val moodEnum = Mood.valueOf(dataPoint.mood)
                val yValue = moodToValueMap[moodEnum] ?: 2f
                val xValue = index.toFloat()
                xValue to yValue
            } catch (e: Exception) {
                Log.e("StatsVM", "Error processing mood data point for chart: $dataPoint", e)
                null
            }
        }
    }

    private fun processMoodSummary(moodEntries: List<MoodDataPoint>): MoodSummary? {
        if (moodEntries.isEmpty()) {
            return MoodSummary(null, emptyMap())
        }
        var totalScore = 0f
        val distribution = mutableMapOf<Mood, Int>()
        moodEntries.forEach { dataPoint ->
            try {
                val moodEnum = Mood.valueOf(dataPoint.mood)
                val score = moodToValueMap[moodEnum] ?: 2f
                totalScore += score
                distribution[moodEnum] = distribution.getOrDefault(moodEnum, 0) + 1
            } catch (e: Exception) {
                Log.e("StatsVM", "Error processing mood data point for summary: $dataPoint", e)
            }
        }
        val averageScore = totalScore / moodEntries.size
        return MoodSummary(averageScore, distribution.toMap())
    }

    private fun processHabitFrequency(
        progressEntries: List<HabitProgressEntity>,
        habits: List<HabitEntity>
    ): List<HabitFrequencyStat> {
        val habitIdToEntityMap = habits.associateBy { it.id }
        val frequencyById = progressEntries
            .groupBy { it.habitId }
            .mapValues { entry -> entry.value.size }
        return frequencyById.mapNotNull { (habitId, count) ->
            val habitEntity = habitIdToEntityMap[habitId]
            habitEntity?.let {
                HabitFrequencyStat(name = it.name, count = count, iconName = it.iconName)
            } ?: run {
                Log.w("StatsVM", "Habit entity not found for ID: $habitId.")
                null
            }
        }.sortedByDescending { it.count }
    }
}
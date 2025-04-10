package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitProgressDao
import com.example.habit_tracker.model.Mood
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter

data class HabitFrequencyStat(
    val name: String,
    val count: Int,
    val iconName: String
)

// Use @OptIn for experimental Flow APIs like flatMapLatest
@OptIn(ExperimentalCoroutinesApi::class)
class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val entryDao: HabitEntryDao = AppDatabase.getDatabase(application).habitEntryDao()
    private val habitDao: HabitDao = AppDatabase.getDatabase(application).habitDao()
    private val progressDao: HabitProgressDao =
        AppDatabase.getDatabase(application).habitProgressDao()

    // --- State for Current Month ---
    private val _currentYearMonth = MutableStateFlow(YearMonth.now()) // Default to current month
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    // --- Mood Data State ---
    private val _moodChartData = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val moodChartData: StateFlow<List<Pair<Float, Float>>> = _moodChartData.asStateFlow()

    private val _showMoodChart = MutableStateFlow(false)
    val showMoodChart: StateFlow<Boolean> = _showMoodChart.asStateFlow()

    private val _isMoodLoading = MutableStateFlow(true) // Renamed for clarity
    val isMoodLoading: StateFlow<Boolean> = _isMoodLoading.asStateFlow()

    // --- Habit Frequency State ---
    private val _habitFrequencyData = MutableStateFlow<List<HabitFrequencyStat>>(emptyList())
    val habitFrequencyData: StateFlow<List<HabitFrequencyStat>> = _habitFrequencyData.asStateFlow()

    private val _isHabitFrequencyLoading = MutableStateFlow(true)
    val isHabitFrequencyLoading: StateFlow<Boolean> = _isHabitFrequencyLoading.asStateFlow()

    private val _showHabitFrequency = MutableStateFlow(false)
    val showHabitFrequency: StateFlow<Boolean> = _showHabitFrequency.asStateFlow()

    // --- Mood Mapping ---
    private val moodToValueMap = mapOf(
        Mood.VERY_BAD to 0f,
        Mood.BAD to 1f,
        Mood.NEUTRAL to 2f,
        Mood.GOOD to 3f,
        Mood.VERY_GOOD to 4f
    )

    // --- Date Formatter ---
    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

    init {
        // Trigger reactive data loading when the month changes
        observeMonthChangesAndLoadData()
    }

    // --- Month Switching Functions ---
    fun showPreviousMonth() {
        _currentYearMonth.value = _currentYearMonth.value.minusMonths(1)
    }

    fun showNextMonth() {
        _currentYearMonth.value = _currentYearMonth.value.plusMonths(1)
    }

    // --- Reactive Data Loading ---
    private fun observeMonthChangesAndLoadData() {
        viewModelScope.launch {
            currentYearMonth.collectLatest { yearMonth ->
                // Trigger loading for both stats whenever the month changes
                loadMoodChartDataForMonth(yearMonth)
                loadHabitFrequencyDataForMonth(yearMonth)
            }
        }
    }

    // Renamed and adapted function
    private fun loadMoodChartDataForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _isMoodLoading.value = true // Start loading mood data

            val startDate = yearMonth.atDay(1)
            val endDate = yearMonth.atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            // Use flatMapLatest to automatically switch to the new month's flow
            // or handle potential errors directly in collectLatest
            entryDao.getMoodEntriesForMonth(startDateString, endDateString)
                .catch { e ->
                    Log.e("StatsVM", "Error loading mood entries for month $yearMonth", e)
                    _moodChartData.value = emptyList()
                    _showMoodChart.value = false
                    _isMoodLoading.value = false
                }
                .collectLatest { moodEntries ->
                    val hasEnoughData =
                        moodEntries.size >= 2 // Still need 2 points for a line/meaningful chart
                    _showMoodChart.value = hasEnoughData

                    if (hasEnoughData) {
                        val chartPoints = moodEntries.mapIndexedNotNull { index, dataPoint ->
                            try {
                                val moodEnum = Mood.valueOf(dataPoint.mood)
                                val yValue = moodToValueMap[moodEnum] ?: 2f
                                val xValue = index.toFloat() // Index within the month's data
                                xValue to yValue
                            } catch (e: Exception) {
                                Log.e("StatsVM", "Error processing mood data point: $dataPoint", e)
                                null
                            }
                        }
                        _moodChartData.value = chartPoints
                    } else {
                        _moodChartData.value = emptyList()
                    }
                    _isMoodLoading.value = false // Finish loading mood data
                }
        }
    }

    // Renamed and adapted function
    private fun loadHabitFrequencyDataForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _isHabitFrequencyLoading.value = true // Start loading habit data

            val startDate = yearMonth.atDay(1)
            val endDate = yearMonth.atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            // Combine habits flow (doesn't change with month) with the progress flow for the specific month
            combine(
                habitDao.getAllHabits(), // This flow only needs to be collected once ideally, but combine handles it
                progressDao.getAllProgressForMonth(
                    startDateString,
                    endDateString
                ) // Use the month-specific query
            ) { habits, progressEntries ->

                val habitIdToEntityMap = habits.associateBy { it.id }
                val frequencyById = progressEntries
                    .groupBy { it.habitId }
                    .mapValues { entry -> entry.value.size }

                val frequencyStatsList = frequencyById.mapNotNull { (habitId, count) ->
                    val habitEntity = habitIdToEntityMap[habitId]
                    if (habitEntity != null) {
                        HabitFrequencyStat(
                            name = habitEntity.name,
                            count = count,
                            iconName = habitEntity.iconName
                        )
                    } else {
                        Log.w(
                            "StatsVM",
                            "Habit entity not found for ID: $habitId during frequency calculation."
                        )
                        null
                    }
                }.sortedByDescending { it.count }

                frequencyStatsList

            }.catch { e ->
                Log.e("StatsVM", "Error loading/processing habit frequency for month $yearMonth", e)
                _habitFrequencyData.value = emptyList()
                _showHabitFrequency.value = false
                _isHabitFrequencyLoading.value = false
                // Optionally emit emptyList() if needed downstream, but setting state directly is often enough
            }.collectLatest { processedFrequencyStatsList ->
                _habitFrequencyData.value = processedFrequencyStatsList
                _showHabitFrequency.value = processedFrequencyStatsList.isNotEmpty()
                _isHabitFrequencyLoading.value = false // Finish loading habit data
            }
        }
    }
}
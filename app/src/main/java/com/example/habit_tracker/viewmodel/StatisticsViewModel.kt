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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter

// Enum for Mode
enum class StatisticsMode {
    MONTHLY, YEARLY
}

enum class HabitCompletionStatus {
    DONE, // Habit was explicitly marked done (progress entry exists)
    NOT_DONE, // An entry exists for the day, but no progress for THIS habit
    NO_ENTRY // No entry exists for this day at all
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

    private val _yearInPixelsData = MutableStateFlow<Map<LocalDate, Mood>>(emptyMap())
    val yearInPixelsData: StateFlow<Map<LocalDate, Mood>> = _yearInPixelsData.asStateFlow()

    private val _isYearInPixelsLoading = MutableStateFlow(true)
    val isYearInPixelsLoading: StateFlow<Boolean> = _isYearInPixelsLoading.asStateFlow()

    private val moodToValueMap = mapOf(
        Mood.VERY_BAD to 0f,
        Mood.BAD to 1f,
        Mood.NEUTRAL to 2f,
        Mood.GOOD to 3f,
        Mood.VERY_GOOD to 4f
    )
    private val isoDateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

    val allHabits: StateFlow<List<HabitEntity>> = habitDao.getAllHabits()
        .catch { e ->
            Log.e("StatsVM", "Error loading all habits", e)
            emit(emptyList()) // Emit empty list on error
        }
        // Convert Flow to StateFlow, share subscription
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Holds the ID of the habit selected by the user for the pixel view
    private val _selectedHabitIdForPixels =
        MutableStateFlow<Int?>(null) // Start with no habit selected
    val selectedHabitIdForPixels: StateFlow<Int?> = _selectedHabitIdForPixels.asStateFlow()

    // Holds the calculated pixel data for the selected habit and year
    private val _habitPixelData =
        MutableStateFlow<Map<LocalDate, HabitCompletionStatus>>(emptyMap())
    val habitPixelData: StateFlow<Map<LocalDate, HabitCompletionStatus>> =
        _habitPixelData.asStateFlow()

    // Loading state specifically for the habit pixel data
    private val _isHabitPixelLoading = MutableStateFlow(false) // Start as false until triggered
    val isHabitPixelLoading: StateFlow<Boolean> = _isHabitPixelLoading.asStateFlow()


    // --- Initialization ---
    init {
        observeTimeRangeChangesAndLoadData()
    }

    // --- Public Control Functions ---
    fun setMode(mode: StatisticsMode) {
        _statisticsMode.value = mode
    }

    fun selectHabitForPixels(habitId: Int?) {
        Log.d("StatsVM", "Habit selected for pixel view: $habitId")
        _selectedHabitIdForPixels.value = habitId
        if (habitId == null) {
            // Clear data if habit is deselected
            _habitPixelData.value = emptyMap()
            _isHabitPixelLoading.value = false
        }
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

    private fun observeTimeRangeChangesAndLoadData() {
        viewModelScope.launch {
            // Combine all state that influences data loading
            combine(
                statisticsMode,
                currentYearMonth,
                currentYear,
                selectedHabitIdForPixels // *** Add selected habit ID ***
            ) { mode, month, year, selectedHabitId ->
                Quadruple(mode, month, year, selectedHabitId)
            }.distinctUntilChanged()
                .collectLatest { (mode, month, year, selectedHabitId) -> // Destructure
                    Log.d(
                        "StatsVM",
                        "Reloading data for Mode: $mode, Time: ${if (mode == StatisticsMode.MONTHLY) month else year}, Selected Habit: $selectedHabitId"
                    )
                    val (startDate, endDate) = calculateDateRange(mode, month, year)

                    // Keep existing calls
                    loadMoodData(mode, startDate, endDate)
                    loadHabitFrequencyData(startDate, endDate)

                    // Load Mood pixels if in yearly mode
                    if (mode == StatisticsMode.YEARLY) {
                        loadYearInPixelsData(year) // Keep this call
                        // *** Trigger habit pixel loading if a habit is selected ***
                        if (selectedHabitId != null) {
                            // We will define loadHabitPixelData next
                            loadHabitPixelData(year, selectedHabitId)
                        } else {
                            // No habit selected, clear data and set loading false
                            _habitPixelData.value = emptyMap()
                            _isHabitPixelLoading.value = false
                        }
                    } else {
                        // Clear yearly pixel data when switching away from yearly mode
                        _yearInPixelsData.value = emptyMap()
                        _isYearInPixelsLoading.value = false
                        _habitPixelData.value = emptyMap()
                        _isHabitPixelLoading.value = false
                    }
                }
        }
    }

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

    private fun loadYearInPixelsData(year: Year) {
        viewModelScope.launch {
            _isYearInPixelsLoading.value = true
            val startDate = year.atDay(1)
            // Ensure correct last day of year calculation
            val endDate = year.atMonth(12).atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "Fetching pixel data for $year ($startDateString to $endDateString)")

            // Use the existing DAO function that gets MoodDataPoints for the range
            entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                .map { moodEntries -> // Transform List<MoodDataPoint> to Map<LocalDate, Mood>
                    Log.d("StatsVM", "Processing ${moodEntries.size} entries for pixel map")
                    val pixelMap = mutableMapOf<LocalDate, Mood>()
                    moodEntries.forEach { dataPoint ->
                        try {
                            val date = LocalDate.parse(dataPoint.date, isoDateFormatter)
                            val mood = Mood.valueOf(dataPoint.mood)
                            pixelMap[date] = mood // Add successfully parsed entry to map
                        } catch (e: Exception) {
                            Log.e("StatsVM", "Error parsing pixel data point: $dataPoint", e)
                            // Decide how to handle errors: skip, use default, etc. Skipping for now.
                        }
                    }
                    pixelMap.toMap() // Return immutable map
                }
                .catch { e ->
                    Log.e("StatsVM", "Error loading year in pixels data for $year", e)
                    emit(emptyMap()) // Emit empty map on error
                }
                .collectLatest { pixelMap ->
                    Log.d("StatsVM", "Pixel map loaded with ${pixelMap.size} entries for $year")
                    _yearInPixelsData.value = pixelMap
                    _isYearInPixelsLoading.value = false
                }
        }
    }

    // --- REPLACE existing loadHabitPixelData with this ---
    private fun loadHabitPixelData(year: Year, habitId: Int) {
        viewModelScope.launch {
            _isHabitPixelLoading.value = true
            val startDate: LocalDate = year.atDay(1)
            // --- Use the correct endDate calculation from loadYearInPixelsData ---
            val endDate: LocalDate = year.atMonth(12).atEndOfMonth()
            // --- End Corrected Calculation ---
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "Fetching habit pixel data for $year, Habit ID: $habitId")

            combine(
                // Flow 1: Set of LocalDates for which any entry exists
                entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                    .map { entries ->
                        entries.mapNotNull { dataPoint ->
                            try {
                                LocalDate.parse(dataPoint.date, isoDateFormatter)
                            } catch (e: Exception) {
                                null
                            }
                        }.toSet()
                    },
                // Flow 2: Map of LocalDate to HabitProgressEntity for the specific habit
                progressDao.getAllProgressBetweenDates(startDateString, endDateString)
                    .map { allProgress ->
                        allProgress
                            .filter { it.habitId == habitId }
                            .mapNotNull { progressEntry ->
                                try {
                                    val date =
                                        LocalDate.parse(progressEntry.entryDate, isoDateFormatter)
                                    date to progressEntry
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .toMap()
                    }
            ) { entryDatesSet: Set<LocalDate>, habitProgressMap: Map<LocalDate, HabitProgressEntity> ->

                Log.d(
                    "StatsVM",
                    "Processing habit pixels: ${entryDatesSet.size} entry dates, ${habitProgressMap.size} progress entries for habit $habitId"
                )

                val pixelMap = mutableMapOf<LocalDate, HabitCompletionStatus>()
                var currentDate: LocalDate = startDate

                while (!currentDate.isAfter(endDate)) {
                    val status = when {
                        entryDatesSet.contains(currentDate) -> {
                            if (habitProgressMap.containsKey(currentDate)) {
                                HabitCompletionStatus.DONE
                            } else {
                                HabitCompletionStatus.NOT_DONE
                            }
                        }

                        else -> HabitCompletionStatus.NO_ENTRY
                    }
                    pixelMap[currentDate] = status
                    currentDate = currentDate.plusDays(1)
                }
                pixelMap.toMap()
            }
                .catch { e ->
                    Log.e(
                        "StatsVM",
                        "Error loading habit pixel data for $year, Habit ID: $habitId",
                        e
                    )
                    emit(emptyMap())
                }
                .collectLatest { pixelMap ->
                    Log.d(
                        "StatsVM",
                        "Habit pixel map loaded with ${pixelMap.size} entries for $year, Habit ID: $habitId"
                    )
                    _habitPixelData.value = pixelMap
                    _isHabitPixelLoading.value = false
                }
        }
    }
    // --- END REPLACEMENT ---

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

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
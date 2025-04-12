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
import com.example.habit_tracker.model.HabitType
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
import org.apache.commons.math3.linear.MatrixUtils
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation
import org.apache.commons.math3.stat.ranking.NaNStrategy
import org.apache.commons.math3.stat.ranking.NaturalRanking
import org.apache.commons.math3.stat.ranking.TiesStrategy
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import kotlin.math.abs

// Enum for Mode
enum class StatisticsMode {
    MONTHLY, YEARLY
}

enum class HabitCompletionStatus {
    DONE_BINARY,       // Binary habit was completed
    DONE_SCALE_LOW,    // Scale habit value 1
    DONE_SCALE_MEDIUM, // Scale habit value 2
    DONE_SCALE_HIGH,   // Scale habit value 3
    NOT_DONE,          // Entry exists, but this habit wasn't done
    NO_ENTRY           // No entry exists for the day
}

data class HabitFrequencyStat(
    val name: String,
    val count: Int,
    val iconName: String
)

data class MoodSummary(
    val averageScore: Float?,
    val distribution: Map<Mood, Int>
)

data class HabitCorrelationResult(
    val habitId: Int,
    val habitName: String,
    val habitIconName: String,
    val coefficient: Double?, // Spearman's Rho
    val pValue: Double?,
    val dataPointCount: Int
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

    private val _allCorrelationResults = MutableStateFlow<List<HabitCorrelationResult>>(emptyList())
    val allCorrelationResults: StateFlow<List<HabitCorrelationResult>> =
        _allCorrelationResults.asStateFlow()

    private val _isAllCorrelationsLoading = MutableStateFlow(false) // Loading flag for the list
    val isAllCorrelationsLoading: StateFlow<Boolean> = _isAllCorrelationsLoading.asStateFlow()

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
            // Combine only the state needed to trigger general reloads
            combine(
                statisticsMode,
                currentYearMonth,
                currentYear
                // Selected Habit for Pixels is handled separately via its select function now
            ) { mode, month, year ->
                Triple(mode, month, year) // Pass mode and time range triggers
            }.distinctUntilChanged()
                .collectLatest { (mode, month, year) ->
                    Log.d(
                        "StatsVM",
                        "Mode/Time Changed: Mode: $mode, Time: ${if (mode == StatisticsMode.MONTHLY) month else year}"
                    )
                    val (startDate, endDate) = calculateDateRange(mode, month, year)

                    // Load base data needed by multiple stats
                    loadMoodData(mode, startDate, endDate)
                    loadHabitFrequencyData(startDate, endDate)

                    // Load yearly-specific data
                    if (mode == StatisticsMode.YEARLY) {
                        loadYearInPixelsData(year) // Mood pixels
                        // Trigger habit pixel loading only if a habit IS selected for pixels
                        // This check should ideally live closer to the selectHabitForPixels logic,
                        // but we keep it here for now based on previous structure.
                        // Re-evaluate if needed.
                        if (_selectedHabitIdForPixels.value != null) {
                            loadHabitPixelData(year, _selectedHabitIdForPixels.value!!)
                        } else {
                            _habitPixelData.value = emptyMap(); _isHabitPixelLoading.value = false
                        }
                    } else {
                        // Clear yearly data when not in yearly mode
                        _yearInPixelsData.value = emptyMap(); _isYearInPixelsLoading.value = false
                        _habitPixelData.value = emptyMap(); _isHabitPixelLoading.value = false
                    }

                    // *** Trigger loading for ALL correlations ***
                    loadAllCorrelations(startDate, endDate) // Pass calculated date range
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

    private fun loadHabitPixelData(year: Year, habitId: Int) {
        viewModelScope.launch {
            _isHabitPixelLoading.value = true
            val startDate: LocalDate = year.atDay(1)
            val endDate: LocalDate = year.atMonth(12).atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "Fetching habit pixel data for $year, Habit ID: $habitId")

            // Combine all necessary flows: All Habits, Entry Dates, Progress for the specific habit
            combine(
                habitDao.getAllHabits(), // Need all habits to find the selected one's type
                entryDao.getMoodEntriesBetweenDates(
                    startDateString,
                    endDateString
                ) // Still using this to get existing entry dates
                    .map { entries ->
                        entries.mapNotNull { dataPoint ->
                            try {
                                LocalDate.parse(dataPoint.date, isoDateFormatter)
                            } catch (e: Exception) {
                                null
                            }
                        }.toSet()
                    },
                progressDao.getAllProgressBetweenDates(
                    startDateString,
                    endDateString
                ) // Get all progress...
                    .map { allProgress -> // ...then filter and map to Date->Progress for the selected habit
                        allProgress
                            .filter { it.habitId == habitId }
                            .mapNotNull { progressEntry ->
                                try {
                                    val date =
                                        LocalDate.parse(progressEntry.entryDate, isoDateFormatter)
                                    date to progressEntry // Pair<LocalDate, HabitProgressEntity>
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .toMap()
                    }
            ) { allHabitsList, entryDatesSet, habitProgressMap -> // Receive all 3 results
                // Find the selected habit's details (needed for type)
                val selectedHabitEntity = allHabitsList.find { it.id == habitId }

                Log.d(
                    "StatsVM",
                    "Processing habit pixels: ${entryDatesSet.size} entry dates, ${habitProgressMap.size} progress entries for habit $habitId (${selectedHabitEntity?.name})"
                )

                val pixelMap = mutableMapOf<LocalDate, HabitCompletionStatus>()
                var currentDate: LocalDate = startDate

                while (!currentDate.isAfter(endDate)) {
                    val status: HabitCompletionStatus = when {
                        // Check if any entry exists for the day
                        entryDatesSet.contains(currentDate) -> {
                            // Check if progress exists for THIS habit on this day
                            val progressEntry = habitProgressMap[currentDate]
                            if (progressEntry != null) {
                                // --- Logic for DONE state ---
                                if (selectedHabitEntity?.type == com.example.habit_tracker.model.HabitType.SCALE) {
                                    // It's a scale habit, check the value
                                    when (progressEntry.value) {
                                        1 -> HabitCompletionStatus.DONE_SCALE_LOW
                                        2 -> HabitCompletionStatus.DONE_SCALE_MEDIUM
                                        3 -> HabitCompletionStatus.DONE_SCALE_HIGH
                                        else -> HabitCompletionStatus.DONE_BINARY // Fallback for unexpected scale value or null
                                    }
                                } else {
                                    // It's a binary habit (or type unknown)
                                    HabitCompletionStatus.DONE_BINARY
                                }
                                // --- End Logic for DONE state ---
                            } else {
                                // Entry exists, but no progress for this habit
                                HabitCompletionStatus.NOT_DONE
                            }
                        }
                        // No entry exists for this day at all
                        else -> HabitCompletionStatus.NO_ENTRY
                    }
                    pixelMap[currentDate] = status
                    currentDate = currentDate.plusDays(1)
                }
                pixelMap.toMap() // Return immutable map
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

    // --- Complete loadAllCorrelations function ---
    private fun loadAllCorrelations(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isAllCorrelationsLoading.value = true
            _allCorrelationResults.value = emptyList() // Clear previous results

            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "Fetching data for all correlations: $startDate - $endDate")

            // Combine flows needed: All Habits, Mood entries, ALL Progress entries
            combine(
                habitDao.getAllHabits(), // Flow<List<HabitEntity>>
                entryDao.getMoodEntriesBetweenDates(
                    startDateString,
                    endDateString
                ), // Flow<List<MoodDataPoint>>
                progressDao.getAllProgressBetweenDates(
                    startDateString,
                    endDateString
                ) // Flow<List<HabitProgressEntity>> - Get ALL progress
            ) { allHabitsList, moodDataList, allProgressList -> // Receive all 3 results

                Log.d(
                    "StatsVM",
                    "Processing all correlations: ${allHabitsList.size} habits, ${moodDataList.size} moods, ${allProgressList.size} progress entries"
                )

                // Pre-process data for efficiency
                val moodMap = moodDataList.associate {
                    try {
                        LocalDate.parse(it.date) to Mood.valueOf(it.mood)
                    } catch (e: Exception) {
                        null to null
                    }
                }.filterKeys { it != null } as Map<LocalDate, Mood>

                val progressByHabitId = allProgressList.groupBy { it.habitId }
                // --- End Pre-processing ---


                val correlationResults = mutableListOf<HabitCorrelationResult>()

                // Iterate through each habit defined in the app
                allHabitsList.forEach { habit ->
                    val habitId = habit.id
                    val habitProgressList =
                        progressByHabitId[habitId] ?: emptyList() // Get progress for this habit
                    val progressMapForHabit =
                        habitProgressList.associate { // Create map for this habit's progress
                            try {
                                LocalDate.parse(it.entryDate) to it
                            } catch (e: Exception) {
                                null to null
                            }
                        }.filterKeys { it != null } as Map<LocalDate, HabitProgressEntity>

                    // Prepare paired lists for THIS habit
                    val moodScores = mutableListOf<Double>()
                    val habitValues = mutableListOf<Double>()
                    val commonDates = moodMap.keys // Use dates where mood was recorded as the basis

                    commonDates.forEach { date ->
                        val mood = moodMap[date]
                        val progress = progressMapForHabit[date] // Progress for THIS habit

                        if (mood != null) { // Ensure mood is valid for this date
                            val moodScore = moodToValueMap[mood]?.toDouble() ?: 2.0
                            val habitValue: Double = if (progress != null) {
                                if (habit.type == HabitType.SCALE) {
                                    progress.value?.toDouble() ?: 1.0
                                } else {
                                    1.0
                                }
                            } else {
                                0.0
                            } // Not done
                            moodScores.add(moodScore)
                            habitValues.add(habitValue)
                        }
                    } // End of commonDates.forEach

                    // --- Calculate Ranks for P-Value ---
                    val rankingAlgorithm = NaturalRanking(NaNStrategy.FAILED, TiesStrategy.AVERAGE)
                    val xArray = moodScores.toDoubleArray() // Original scores array
                    val yArray = habitValues.toDoubleArray() // Original values array
                    val xRanks = try {
                        rankingAlgorithm.rank(xArray)
                    } catch (e: Exception) {
                        Log.e("StatsVM", "Error ranking moodScores for ${habit.name}", e); null
                    }
                    val yRanks = try {
                        rankingAlgorithm.rank(yArray)
                    } catch (e: Exception) {
                        Log.e("StatsVM", "Error ranking habitValues for ${habit.name}", e); null
                    }
                    // --- End Rank Calculation ---

                    // Now calculate correlation and p-value
                    val dataPointCount = moodScores.size
                    // Check if ranking succeeded and we have enough data points
                    if (dataPointCount >= 3 && xRanks != null && yRanks != null) {
                        try {
                            // Check for variance using ORIGINAL scores
                            val moodVariance = moodScores.distinct().size > 1
                            val habitVariance = habitValues.distinct().size > 1

                            if (!moodVariance || !habitVariance) {
                                Log.d(
                                    "StatsVM",
                                    "Skipping correlation for ${habit.name}: No variance in original data"
                                )
                            } else {
                                // Calculate Spearman's Rho (uses original data arrays)
                                val spearmanRho = SpearmansCorrelation().correlation(xArray, yArray)

                                // Calculate P-Value using Pearson's on Ranks via Constructor
                                var pValue: Double? = null
                                try {
                                    val dataMatrix = MatrixUtils.createRealMatrix(dataPointCount, 2)
                                    dataMatrix.setColumn(0, xRanks) // Use calculated xRanks
                                    dataMatrix.setColumn(1, yRanks) // Use calculated yRanks
                                    val pearsonOnRanks = PearsonsCorrelation(dataMatrix)
                                    val pValueMatrix =
                                        pearsonOnRanks.correlationPValues // Get p-value matrix

                                    if (pValueMatrix != null && pValueMatrix.rowDimension > 1 && pValueMatrix.columnDimension > 1) {
                                        pValue = pValueMatrix.getEntry(
                                            0,
                                            1
                                        ) // Get p-value for mood vs habit
                                    } else {
                                        Log.w(
                                            "StatsVM",
                                            "P-value matrix from Pearson constructor was null or too small for ${habit.name}"
                                        )
                                    }
                                } catch (pError: Exception) {
                                    Log.e(
                                        "StatsVM",
                                        "Error calculating Pearson/P-value on ranks for ${habit.name}",
                                        pError
                                    )
                                    pValue = null // Ensure pValue is null on error
                                }
                                // End P-Value Calculation

                                // Add result only if calculation was successful and values are finite
                                if (spearmanRho.isFinite() && pValue != null && pValue.isFinite()) {
                                    correlationResults.add(
                                        HabitCorrelationResult(
                                            habitId = habitId,
                                            habitName = habit.name,
                                            habitIconName = habit.iconName,
                                            coefficient = spearmanRho,
                                            pValue = pValue, // Store p-value
                                            dataPointCount = dataPointCount
                                        )
                                    )
                                    Log.d(
                                        "StatsVM",
                                        "Correlation for ${habit.name}: rho=$spearmanRho, p=$pValue ($dataPointCount points)"
                                    )
                                } else {
                                    Log.w(
                                        "StatsVM",
                                        "Skipping correlation for ${habit.name}: Result or p-value not finite (rho=$spearmanRho, p=$pValue)"
                                    )
                                }
                            } // End variance check
                        } catch (e: Exception) { // Catch errors during correlation/p-value calculation
                            Log.e(
                                "StatsVM",
                                "Error calculating Spearman/P-value for ${habit.name}",
                                e
                            )
                        }
                    } else { // Handle insufficient data OR ranking failure
                        Log.d(
                            "StatsVM",
                            "Skipping correlation for ${habit.name}: Not enough data ($dataPointCount points) or ranking failed."
                        )
                    }
                    // --- End Correlation & P-value Calculation Block ---

                } // End forEach habit

                // Sort results by p-value ascending, then by absolute rho descending
                correlationResults.sortWith(compareBy<HabitCorrelationResult> {
                    it.pValue ?: Double.MAX_VALUE
                }.thenByDescending { abs(it.coefficient ?: 0.0) })
                correlationResults // Return the final sorted list

            }.catch { e -> // Catch errors from combine or the flows within it
                Log.e("StatsVM", "Error combining flows for all correlations data", e)
                emit(emptyList<HabitCorrelationResult>().toMutableList()) // Emit explicitly typed mutable empty list
            }.collectLatest { results -> // results will be List<HabitCorrelationResult>
                Log.d(
                    "StatsVM",
                    "Finished calculating all correlations. Found ${results.size} valid results."
                )
                _allCorrelationResults.value =
                    results // Update state with results or empty list from catch
                _isAllCorrelationsLoading.value = false // Loading finished
            }
        }
    }
    // --- END loadAllCorrelations function ---

    private fun interpretCorrelation(rho: Double?): String {
        return when {
            rho == null || !rho.isFinite() -> "Calculation error."
            abs(rho) >= 0.7 -> if (rho > 0) "Strong positive" else "Strong negative"
            abs(rho) >= 0.4 -> if (rho > 0) "Moderate positive" else "Moderate negative"
            abs(rho) >= 0.1 -> if (rho > 0) "Weak positive" else "Weak negative"
            else -> "No significant correlation"
        }
    }

    data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
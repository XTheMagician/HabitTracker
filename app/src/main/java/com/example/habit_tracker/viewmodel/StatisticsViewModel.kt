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

    // --- Year In Pixels (Mood) ---
    private val _yearInPixelsData = MutableStateFlow<Map<LocalDate, Mood>>(emptyMap())
    val yearInPixelsData: StateFlow<Map<LocalDate, Mood>> = _yearInPixelsData.asStateFlow()

    private val _isYearInPixelsLoading = MutableStateFlow(true)
    val isYearInPixelsLoading: StateFlow<Boolean> = _isYearInPixelsLoading.asStateFlow()

    // --- Habit Correlation ---
    private val _allCorrelationResults = MutableStateFlow<List<HabitCorrelationResult>>(emptyList())
    val allCorrelationResults: StateFlow<List<HabitCorrelationResult>> =
        _allCorrelationResults.asStateFlow()

    private val _isAllCorrelationsLoading = MutableStateFlow(false)
    val isAllCorrelationsLoading: StateFlow<Boolean> = _isAllCorrelationsLoading.asStateFlow()

    // --- Habit Year In Pixels ---
    val allHabits: StateFlow<List<HabitEntity>> = habitDao.getAllHabits()
        .catch { e ->
            Log.e("StatsVM", "Error loading all habits", e)
            emit(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedHabitIdForPixels = MutableStateFlow<Int?>(null)
    val selectedHabitIdForPixels: StateFlow<Int?> = _selectedHabitIdForPixels.asStateFlow()

    private val _habitPixelData =
        MutableStateFlow<Map<LocalDate, HabitCompletionStatus>>(emptyMap())
    val habitPixelData: StateFlow<Map<LocalDate, HabitCompletionStatus>> =
        _habitPixelData.asStateFlow()

    private val _isHabitPixelLoading = MutableStateFlow(false)
    val isHabitPixelLoading: StateFlow<Boolean> = _isHabitPixelLoading.asStateFlow()

    // --- Internal Helpers ---
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
        val previousMode = _statisticsMode.value
        if (previousMode == mode) return // No change

        _statisticsMode.value = mode

        // If switching TO yearly mode, trigger habit pixel load if a habit is selected
        if (mode == StatisticsMode.YEARLY) {
            loadHabitPixelDataForSelectedHabitAndYear() // Safe, checks internally
        } else {
            // If switching away from yearly, clear the habit pixel data
            _habitPixelData.value = emptyMap()
            _isHabitPixelLoading.value = false
        }
        // General data loading is handled by observeTimeRangeChangesAndLoadData
    }

    fun selectHabitForPixels(habitId: Int?) {
        val oldHabitId = _selectedHabitIdForPixels.value
        if (oldHabitId == habitId) return // No actual change

        Log.d("StatsVM", "Habit selected for pixel view: $habitId")
        _selectedHabitIdForPixels.value = habitId // Update the state

        // Trigger loading ONLY if mode is Yearly
        if (_statisticsMode.value == StatisticsMode.YEARLY) {
            loadHabitPixelDataForSelectedHabitAndYear() // Safe, checks internally
        } else {
            // If not in yearly mode, ensure data is cleared (or just do nothing, it won't be shown)
            _habitPixelData.value = emptyMap()
            _isHabitPixelLoading.value = false
        }
    }

    fun showPreviousTimePeriod() {
        when (_statisticsMode.value) {
            StatisticsMode.MONTHLY -> _currentYearMonth.value =
                _currentYearMonth.value.minusMonths(1)

            StatisticsMode.YEARLY -> {
                _currentYear.value = _currentYear.value.minusYears(1)
                // Reload habit pixels for the new year if in yearly mode
                loadHabitPixelDataForSelectedHabitAndYear() // Safe, checks internally
            }
        }
        // General data loading is handled by observeTimeRangeChangesAndLoadData
    }

    fun showNextTimePeriod() {
        when (_statisticsMode.value) {
            StatisticsMode.MONTHLY -> _currentYearMonth.value =
                _currentYearMonth.value.plusMonths(1)

            StatisticsMode.YEARLY -> {
                _currentYear.value = _currentYear.value.plusYears(1)
                // Reload habit pixels for the new year if in yearly mode
                loadHabitPixelDataForSelectedHabitAndYear() // Safe, checks internally
            }
        }
        // General data loading is handled by observeTimeRangeChangesAndLoadData
    }

    // --- Data Loading Orchestration ---
    private fun observeTimeRangeChangesAndLoadData() {
        viewModelScope.launch {
            // Combine flows that trigger general reloads based on time/mode
            combine(
                statisticsMode,
                currentYearMonth,
                currentYear
            ) { mode, month, year ->
                Triple(mode, month, year)
            }.distinctUntilChanged()
                .collectLatest { (mode, month, year) ->
                    Log.d(
                        "StatsVM",
                        "Mode/Time Changed Trigger: Mode: $mode, Time: ${if (mode == StatisticsMode.MONTHLY) month else year}"
                    )
                    val (startDate, endDate) = calculateDateRange(mode, month, year)

                    // Load data common to both modes or mode-dependent but based on time range
                    loadMoodData(mode, startDate, endDate)
                    loadHabitFrequencyData(startDate, endDate)
                    loadAllCorrelations(startDate, endDate) // Correlations depend on the range

                    // Load data specific to Yearly mode (excluding habit pixels which are handled separately)
                    if (mode == StatisticsMode.YEARLY) {
                        loadYearInPixelsData(year) // Mood pixels
                    } else {
                        // Clear yearly-specific data when not in yearly mode
                        _yearInPixelsData.value = emptyMap(); _isYearInPixelsLoading.value = false
                        // Habit pixels are cleared by setMode/selectHabit when necessary
                    }
                }
        }
    }

    // --- Specific Data Loaders ---

    private fun calculateDateRange(
        mode: StatisticsMode,
        month: YearMonth,
        year: Year
    ): Pair<LocalDate, LocalDate> {
        return when (mode) {
            StatisticsMode.MONTHLY -> month.atDay(1) to month.atEndOfMonth()
            StatisticsMode.YEARLY -> year.atDay(1) to year.atMonth(12).atEndOfMonth()
        }
    }

    private fun loadMoodData(mode: StatisticsMode, startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isMoodLoading.value = true
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)
            entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                .catch { e ->
                    Log.e("StatsVM", "Error loading mood entries for $startDate - $endDate", e)
                    _moodChartData.value = emptyList()
                    _moodSummaryData.value = null
                    _showMoodChart.value = false
                    _isMoodLoading.value = false
                }
                .collectLatest { moodEntries ->
                    _moodSummaryData.value = processMoodSummary(moodEntries)
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

    private fun loadHabitFrequencyData(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isHabitFrequencyLoading.value = true
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)
            combine(
                habitDao.getAllHabits(),
                progressDao.getAllProgressBetweenDates(startDateString, endDateString)
            ) { habits, progressEntries ->
                processHabitFrequency(progressEntries, habits)
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
            val endDate = year.atMonth(12).atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)
            entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                .map { moodEntries ->
                    moodEntries.mapNotNull { dataPoint ->
                        try {
                            LocalDate.parse(dataPoint.date, isoDateFormatter) to Mood.valueOf(
                                dataPoint.mood
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }.toMap()
                }
                .catch { e ->
                    Log.e("StatsVM", "Error loading year in pixels data for $year", e)
                    emit(emptyMap())
                }
                .collectLatest { pixelMap ->
                    _yearInPixelsData.value = pixelMap
                    _isYearInPixelsLoading.value = false
                }
        }
    }

    // *** REVISED: Loads data based on current state ***
    private fun loadHabitPixelDataForSelectedHabitAndYear() {
        // Read current state values
        val habitId = _selectedHabitIdForPixels.value
        val year = _currentYear.value
        val mode = _statisticsMode.value

        // Exit if no habit selected or not in yearly mode
        if (habitId == null || mode != StatisticsMode.YEARLY) {
            if (habitId == null) { // Ensure data is cleared if habit ID is null
                _habitPixelData.value = emptyMap()
            }
            _isHabitPixelLoading.value = false // Ensure loading indicator is off
            return
        }

        viewModelScope.launch {
            _isHabitPixelLoading.value = true
            val startDate: LocalDate = year.atDay(1)
            val endDate: LocalDate = year.atMonth(12).atEndOfMonth()
            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "[REVISED] Fetching habit pixel data for $year, Habit ID: $habitId")

            combine(
                habitDao.getAllHabits(),
                entryDao.getMoodEntriesBetweenDates(startDateString, endDateString)
                    .map { entries ->
                        entries.mapNotNull { dp ->
                            try {
                                LocalDate.parse(dp.date)
                            } catch (e: Exception) {
                                null
                            }
                        }.toSet()
                    },
                progressDao.getAllProgressBetweenDates(startDateString, endDateString)
                    .map { allProgress ->
                        allProgress.filter { it.habitId == habitId }
                            .mapNotNull { pe ->
                                try {
                                    LocalDate.parse(pe.entryDate) to pe
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .toMap()
                    }
            ) { allHabitsList, entryDatesSet, habitProgressMap ->
                val selectedHabitEntity = allHabitsList.find { it.id == habitId }
                Log.d(
                    "StatsVM",
                    "[REVISED] Processing habit pixels: ${entryDatesSet.size} entry dates, ${habitProgressMap.size} progress entries for habit $habitId"
                )

                val pixelMap = mutableMapOf<LocalDate, HabitCompletionStatus>()
                var currentDate: LocalDate = startDate
                while (!currentDate.isAfter(endDate)) {
                    pixelMap[currentDate] = when {
                        entryDatesSet.contains(currentDate) -> {
                            val progressEntry = habitProgressMap[currentDate]
                            if (progressEntry != null) {
                                if (selectedHabitEntity?.type == HabitType.SCALE) {
                                    when (progressEntry.value) {
                                        1 -> HabitCompletionStatus.DONE_SCALE_LOW
                                        2 -> HabitCompletionStatus.DONE_SCALE_MEDIUM
                                        3 -> HabitCompletionStatus.DONE_SCALE_HIGH
                                        else -> HabitCompletionStatus.DONE_BINARY
                                    }
                                } else {
                                    HabitCompletionStatus.DONE_BINARY
                                }
                            } else {
                                HabitCompletionStatus.NOT_DONE
                            }
                        }

                        else -> HabitCompletionStatus.NO_ENTRY
                    }
                    currentDate = currentDate.plusDays(1)
                }
                pixelMap.toMap()
            }
                .catch { e ->
                    Log.e(
                        "StatsVM",
                        "[REVISED] Error loading habit pixel data for $year, Habit ID: $habitId",
                        e
                    )
                    emit(emptyMap())
                }
                .collectLatest { pixelMap ->
                    Log.d(
                        "StatsVM",
                        "[REVISED] Habit pixel map loaded with ${pixelMap.size} entries for $year, Habit ID: $habitId"
                    )
                    _habitPixelData.value = pixelMap
                    _isHabitPixelLoading.value = false
                }
        }
    }


    private fun loadAllCorrelations(startDate: LocalDate, endDate: LocalDate) {
        viewModelScope.launch {
            _isAllCorrelationsLoading.value = true
            _allCorrelationResults.value = emptyList() // Clear previous

            val startDateString = startDate.format(isoDateFormatter)
            val endDateString = endDate.format(isoDateFormatter)

            Log.d("StatsVM", "Fetching data for all correlations: $startDate - $endDate")

            combine(
                habitDao.getAllHabits(),
                entryDao.getMoodEntriesBetweenDates(startDateString, endDateString),
                progressDao.getAllProgressBetweenDates(startDateString, endDateString)
            ) { allHabitsList, moodDataList, allProgressList ->

                Log.d(
                    "StatsVM",
                    "Processing all correlations: ${allHabitsList.size} habits, ${moodDataList.size} moods, ${allProgressList.size} progress entries"
                )

                val moodMap = moodDataList.associate {
                    try {
                        LocalDate.parse(it.date) to Mood.valueOf(it.mood)
                    } catch (e: Exception) {
                        null to null
                    }
                }.filterKeys { it != null } as Map<LocalDate, Mood>
                val progressByHabitId = allProgressList.groupBy { it.habitId }
                val correlationResults = mutableListOf<HabitCorrelationResult>()

                allHabitsList.forEach { habit ->
                    val habitId = habit.id
                    val habitProgressList = progressByHabitId[habitId] ?: emptyList()
                    val progressMapForHabit = habitProgressList.associate {
                        try {
                            LocalDate.parse(it.entryDate) to it
                        } catch (e: Exception) {
                            null to null
                        }
                    }.filterKeys { it != null } as Map<LocalDate, HabitProgressEntity>

                    val moodScores = mutableListOf<Double>()
                    val habitValues = mutableListOf<Double>()
                    val commonDates = moodMap.keys

                    commonDates.forEach { date ->
                        val mood = moodMap[date]
                        val progress = progressMapForHabit[date]
                        if (mood != null) {
                            val moodScore = moodToValueMap[mood]?.toDouble() ?: 2.0
                            val habitValue: Double = if (progress != null) {
                                if (habit.type == HabitType.SCALE) progress.value?.toDouble()
                                    ?: 1.0 else 1.0
                            } else 0.0
                            moodScores.add(moodScore)
                            habitValues.add(habitValue)
                        }
                    }

                    val rankingAlgorithm = NaturalRanking(NaNStrategy.FAILED, TiesStrategy.AVERAGE)
                    val xArray = moodScores.toDoubleArray()
                    val yArray = habitValues.toDoubleArray()
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

                    val dataPointCount = moodScores.size
                    if (dataPointCount >= 3 && xRanks != null && yRanks != null) {
                        try {
                            val moodVariance = moodScores.distinct().size > 1
                            val habitVariance = habitValues.distinct().size > 1
                            if (!moodVariance || !habitVariance) {
                                Log.d(
                                    "StatsVM",
                                    "Skipping correlation for ${habit.name}: No variance in original data"
                                )
                            } else {
                                val spearmanRho = SpearmansCorrelation().correlation(xArray, yArray)
                                var pValue: Double? = null
                                try {
                                    val dataMatrix = MatrixUtils.createRealMatrix(
                                        dataPointCount,
                                        2
                                    ); dataMatrix.setColumn(0, xRanks); dataMatrix.setColumn(
                                        1,
                                        yRanks
                                    )
                                    val pearsonOnRanks = PearsonsCorrelation(dataMatrix)
                                    val pValueMatrix = pearsonOnRanks.correlationPValues
                                    if (pValueMatrix != null && pValueMatrix.rowDimension > 1 && pValueMatrix.columnDimension > 1) {
                                        pValue = pValueMatrix.getEntry(0, 1)
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
                                    ); pValue = null
                                }

                                if (spearmanRho.isFinite() && pValue != null && pValue.isFinite()) {
                                    correlationResults.add(
                                        HabitCorrelationResult(
                                            habitId = habitId,
                                            habitName = habit.name,
                                            habitIconName = habit.iconName,
                                            coefficient = spearmanRho,
                                            pValue = pValue,
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
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "StatsVM",
                                "Error calculating Spearman/P-value for ${habit.name}",
                                e
                            )
                        }
                    } else {
                        Log.d(
                            "StatsVM",
                            "Skipping correlation for ${habit.name}: Not enough data ($dataPointCount points) or ranking failed."
                        )
                    }
                }
                correlationResults.sortWith(compareBy<HabitCorrelationResult> {
                    it.pValue ?: Double.MAX_VALUE
                }.thenByDescending { abs(it.coefficient ?: 0.0) })
                correlationResults // Return final list
            }.catch { e ->
                Log.e("StatsVM", "Error combining flows for all correlations data", e)
                emit(emptyList<HabitCorrelationResult>().toMutableList()) // Emit typed empty list
            }.collectLatest { results ->
                Log.d(
                    "StatsVM",
                    "Finished calculating all correlations. Found ${results.size} valid results."
                )
                _allCorrelationResults.value = results
                _isAllCorrelationsLoading.value = false
            }
        }
    }

    // --- Processing Helpers ---
    private fun processMoodChartData(moodEntries: List<MoodDataPoint>): List<Pair<Float, Float>> {
        return moodEntries.mapIndexedNotNull { index, dataPoint ->
            try {
                val moodEnum = Mood.valueOf(dataPoint.mood)
                (index.toFloat()) to (moodToValueMap[moodEnum] ?: 2f)
            } catch (e: Exception) {
                null
            }
        }
    }

    private fun processMoodSummary(moodEntries: List<MoodDataPoint>): MoodSummary? {
        if (moodEntries.isEmpty()) return MoodSummary(null, emptyMap())
        val distribution = mutableMapOf<Mood, Int>()
        val totalScore = moodEntries.sumOf { dataPoint ->
            try {
                val moodEnum = Mood.valueOf(dataPoint.mood)
                distribution[moodEnum] = distribution.getOrDefault(moodEnum, 0) + 1
                moodToValueMap[moodEnum]?.toDouble() ?: 2.0
            } catch (e: Exception) {
                2.0
            } // Default score on error
        }
        return MoodSummary((totalScore / moodEntries.size).toFloat(), distribution.toMap())
    }

    private fun processHabitFrequency(
        progressEntries: List<HabitProgressEntity>,
        habits: List<HabitEntity>
    ): List<HabitFrequencyStat> {
        val habitIdToEntityMap = habits.associateBy { it.id }
        return progressEntries
            .groupBy { it.habitId }
            .mapNotNull { (habitId, entries) ->
                habitIdToEntityMap[habitId]?.let {
                    HabitFrequencyStat(name = it.name, count = entries.size, iconName = it.iconName)
                }
            }
            .sortedByDescending { it.count }
    }

    // Unused interpretCorrelation function removed for brevity unless needed elsewhere
    // data class Quadruple removed as it's unused
}
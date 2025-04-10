package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitProgressDao
import com.example.habit_tracker.data.db.MoodDataPoint
import com.example.habit_tracker.model.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Define the data class here
data class HabitFrequencyStat(
    val name: String,
    val count: Int,
    val iconName: String
)

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val entryDao: HabitEntryDao = AppDatabase.getDatabase(application).habitEntryDao()
    private val habitDao: HabitDao = AppDatabase.getDatabase(application).habitDao()
    private val progressDao: HabitProgressDao =
        AppDatabase.getDatabase(application).habitProgressDao()

    private val _moodChartData = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val moodChartData: StateFlow<List<Pair<Float, Float>>> = _moodChartData.asStateFlow()

    private val _showMoodChart = MutableStateFlow(false)
    val showMoodChart: StateFlow<Boolean> = _showMoodChart.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Update StateFlow type to use HabitFrequencyStat
    private val _habitFrequencyData = MutableStateFlow<List<HabitFrequencyStat>>(emptyList())
    val habitFrequencyData: StateFlow<List<HabitFrequencyStat>> = _habitFrequencyData.asStateFlow()

    private val _isHabitFrequencyLoading = MutableStateFlow(true)
    val isHabitFrequencyLoading: StateFlow<Boolean> = _isHabitFrequencyLoading.asStateFlow()

    private val _showHabitFrequency = MutableStateFlow(false)
    val showHabitFrequency: StateFlow<Boolean> = _showHabitFrequency.asStateFlow()

    private val moodToValueMap = mapOf(
        Mood.VERY_BAD to 0f,
        Mood.BAD to 1f,
        Mood.NEUTRAL to 2f,
        Mood.GOOD to 3f,
        Mood.VERY_GOOD to 4f
    )

    init {
        loadMoodChartData()
        loadHabitFrequencyData()
    }

    private fun loadMoodChartData() {
        viewModelScope.launch {
            _isLoading.value = true

            val startDate = LocalDate.now().minusDays(30)
            val startDateString = startDate.format(DateTimeFormatter.ISO_DATE)

            try {
                entryDao.getMoodEntriesSince(startDateString)
                    .collectLatest { moodEntries: List<MoodDataPoint> ->
                        val hasEnoughData = moodEntries.size >= 2
                        _showMoodChart.value = hasEnoughData

                        if (hasEnoughData) {
                            val chartPoints = moodEntries.mapIndexedNotNull { index, dataPoint ->
                                try {
                                    val moodEnum = Mood.valueOf(dataPoint.mood)
                                    val yValue = moodToValueMap[moodEnum] ?: 2f
                                    val xValue = index.toFloat()
                                    xValue to yValue
                                } catch (e: Exception) {
                                    Log.e("StatsVM", "Error processing data point: $dataPoint", e)
                                    null
                                }
                            }
                            _moodChartData.value = chartPoints
                        } else {
                            _moodChartData.value = emptyList()
                        }
                        _isLoading.value = false
                    }
            } catch (e: Exception) {
                Log.e("StatsVM", "Error loading mood entries", e)
                _moodChartData.value = emptyList()
                _showMoodChart.value = false
                _isLoading.value = false
            }
        }
    }

    private fun loadHabitFrequencyData() {
        viewModelScope.launch {
            _isHabitFrequencyLoading.value = true
            val startDate = LocalDate.now().minusDays(30)
            val startDateString = startDate.format(DateTimeFormatter.ISO_DATE)

            combine(
                habitDao.getAllHabits(),
                progressDao.getAllProgressSince(startDateString)
            ) { habits, progressEntries ->

                // Change to map ID to full HabitEntity
                val habitIdToEntityMap = habits.associateBy { it.id }

                val frequencyById = progressEntries
                    .groupBy { it.habitId }
                    .mapValues { entry -> entry.value.size }

                // Change to map to HabitFrequencyStat
                val frequencyStatsList = frequencyById.mapNotNull { (habitId, count) ->
                    val habitEntity = habitIdToEntityMap[habitId]
                    if (habitEntity != null) {
                        HabitFrequencyStat(
                            name = habitEntity.name,
                            count = count,
                            iconName = habitEntity.iconName // Include iconName
                        )
                    } else {
                        Log.w(
                            "StatsVM",
                            "Habit entity not found for ID: $habitId during frequency calculation."
                        )
                        null
                    }
                }.sortedByDescending { it.count }

                frequencyStatsList // Return the List<HabitFrequencyStat>

            }.catch { e ->
                Log.e("StatsVM", "Error loading or processing habit frequency data", e)
                _habitFrequencyData.value = emptyList()
                _showHabitFrequency.value = false
                _isHabitFrequencyLoading.value = false
                emit(emptyList<HabitFrequencyStat>()) // Emit typed empty list
            }.collectLatest { processedFrequencyStatsList ->
                _habitFrequencyData.value = processedFrequencyStatsList
                _showHabitFrequency.value = processedFrequencyStatsList.isNotEmpty()
                _isHabitFrequencyLoading.value = false
            }
        }
    }
}
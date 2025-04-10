package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.MoodDataPoint
import com.example.habit_tracker.model.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StatisticsViewModel(application: Application) : AndroidViewModel(application) {

    private val entryDao: HabitEntryDao = AppDatabase.getDatabase(application).habitEntryDao()

    // StateFlow to hold the processed data points for the chart (X=index, Y=mood value)
    private val _moodChartData = MutableStateFlow<List<Pair<Float, Float>>>(emptyList())
    val moodChartData: StateFlow<List<Pair<Float, Float>>> = _moodChartData.asStateFlow()

    // StateFlow to indicate if there's enough data to show the chart
    private val _showMoodChart = MutableStateFlow(false)
    val showMoodChart: StateFlow<Boolean> = _showMoodChart.asStateFlow()

    // Optional: StateFlow for loading indicator
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Map Mood enum to numerical value for plotting (adjust values if needed)
    private val moodToValueMap = mapOf(
        Mood.VERY_BAD to 0f,
        Mood.BAD to 1f,
        Mood.NEUTRAL to 2f,
        Mood.GOOD to 3f,
        Mood.VERY_GOOD to 4f
    )

    init {
        loadMoodChartData()
    }

    private fun loadMoodChartData() {
        viewModelScope.launch {
            _isLoading.value = true // Start loading

            // Calculate start date (e.g., 30 days ago)
            val startDate = LocalDate.now().minusDays(30)
            val startDateString = startDate.format(DateTimeFormatter.ISO_DATE)

            try {
                entryDao.getMoodEntriesSince(startDateString)
                    .collectLatest { moodEntries: List<MoodDataPoint> -> // Collect the flow

                        // Check if enough data points exist (e.g., at least 2 for a line)
                        val hasEnoughData = moodEntries.size >= 2
                        _showMoodChart.value = hasEnoughData

                        if (hasEnoughData) {
                            // Process data points for the chart
                            val chartPoints = moodEntries.mapIndexedNotNull { index, dataPoint ->
                                try {
                                    // Convert mood string from DB back to enum, then to float value
                                    val moodEnum = Mood.valueOf(dataPoint.mood)
                                    val yValue = moodToValueMap[moodEnum]
                                        ?: 2f // Default to Neutral if unknown

                                    // Use simple index for X-axis
                                    val xValue = index.toFloat()

                                    // TODO Advanced: Could calculate days since start date for X if needed
                                    // val entryDate = LocalDate.parse(dataPoint.date)
                                    // val xValue = ChronoUnit.DAYS.between(startDate, entryDate).toFloat()

                                    xValue to yValue // Create Pair<Float, Float>
                                } catch (e: Exception) {
                                    Log.e("StatsVM", "Error processing data point: $dataPoint", e)
                                    null // Skip invalid points
                                }
                            }
                            _moodChartData.value = chartPoints
                        } else {
                            _moodChartData.value = emptyList() // Clear data if not enough points
                        }
                        _isLoading.value = false // Finish loading
                    }
            } catch (e: Exception) {
                Log.e("StatsVM", "Error loading mood entries", e)
                _moodChartData.value = emptyList()
                _showMoodChart.value = false
                _isLoading.value = false // Finish loading even on error
            }
        }
    }
}
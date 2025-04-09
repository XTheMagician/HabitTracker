package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitEntryEntity
import com.example.habit_tracker.data.db.HabitProgressDao
import com.example.habit_tracker.data.db.HabitProgressEntity
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.toEntity
import com.example.habit_tracker.model.toUiModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalCoroutinesApi::class)
class HabitEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val entryDao: HabitEntryDao = db.habitEntryDao()
    private val habitDao: HabitDao = db.habitDao()
    private val progressDao: HabitProgressDao = db.habitProgressDao()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    val entriesForCurrentMonth: StateFlow<List<HabitEntry>> = _currentYearMonth
        .flatMapLatest { yearMonth ->
            entryDao.getAllEntries().map { allEntries ->
                allEntries.filter { entryEntity ->
                    try {
                        YearMonth.from(LocalDate.parse(entryEntity.date)) == yearMonth
                    } catch (e: Exception) {
                        false
                    }
                }
            }
        }
        .flatMapLatest { monthlyEntryEntities ->
            if (monthlyEntryEntities.isEmpty()) {
                flowOf(emptyList())
            } else {
                habitDao.getAllHabits().flatMapLatest { allHabitEntities ->
                    val habitsMap = allHabitEntities.associateBy { it.id }
                    val progressFlows: List<Flow<Pair<HabitEntryEntity, List<HabitProgressEntity>>>> =
                        monthlyEntryEntities.map { entryEntity ->
                            progressDao.getHabitProgressForDate(entryEntity.date)
                                .map { progressList -> entryEntity to progressList }
                        }

                    combine(progressFlows) { results ->
                        results.mapNotNull { (entryEntity, progressEntities) ->
                            val habitProgressList = progressEntities.mapNotNull { progressEntity ->
                                habitsMap[progressEntity.habitId]?.let { habitEntity ->
                                    HabitProgress(
                                        habit = habitEntity.toUiModel(),
                                        value = progressEntity.value
                                    )
                                }
                            }
                            try {
                                HabitEntry(
                                    date = LocalDate.parse(entryEntity.date),
                                    mood = Mood.valueOf(entryEntity.mood),
                                    habits = habitProgressList
                                )
                            } catch (e: Exception) {
                                Log.e("ViewModelMap", "Error mapping entry ${entryEntity.date}", e)
                                null
                            }
                        }.sortedByDescending { it.date }
                    }
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun showPreviousMonth() {
        _currentYearMonth.update { it.minusMonths(1) }
    }

    fun showNextMonth() {
        _currentYearMonth.update { it.plusMonths(1) }
    }

    fun saveEntry(entry: HabitEntry) {
        viewModelScope.launch {
            entryDao.insertOrUpdate(entry.toEntity())
            val dateString = entry.date.format(DateTimeFormatter.ISO_DATE)
            progressDao.deleteAllProgressForDate(dateString)
            entry.habits.forEach { habitProgress ->
                val progressEntity = HabitProgressEntity(
                    entryDate = dateString,
                    habitId = habitProgress.habit.id,
                    value = habitProgress.value
                )
                progressDao.insertOrUpdateProgress(progressEntity)
            }
        }
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            entryDao.deleteByDate(date.format(DateTimeFormatter.ISO_DATE))
        }
    }

    suspend fun getEntryByDate(date: LocalDate): HabitEntry? {
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        val entryEntity = entryDao.getEntryByDateOnce(dateString) ?: return null
        val progressEntities = progressDao.getProgressForDateOnce(dateString) ?: emptyList()

        if (progressEntities.isEmpty()) {
            try {
                return HabitEntry(
                    date = LocalDate.parse(entryEntity.date),
                    mood = Mood.valueOf(entryEntity.mood),
                    habits = emptyList()
                )
            } catch (e: Exception) {
                Log.e(
                    "HabitEntryViewModel",
                    "getEntryByDate - Could not parse entry: ${entryEntity.date}",
                    e
                )
                return null
            }
        }

        val habitIds = progressEntities.map { it.habitId }.distinct()
        val habitsMap = habitDao.getHabitsByIdsOnce(habitIds).associateBy { it.id }

        val habitProgressList = progressEntities.mapNotNull { progressEntity ->
            habitsMap[progressEntity.habitId]?.let { habitEntity ->
                HabitProgress(
                    habit = habitEntity.toUiModel(),
                    value = progressEntity.value
                )
            }
        }

        try {
            return HabitEntry(
                date = LocalDate.parse(entryEntity.date),
                mood = Mood.valueOf(entryEntity.mood),
                habits = habitProgressList
            )
        } catch (e: Exception) {
            Log.e(
                "HabitEntryViewModel",
                "getEntryByDate - Could not parse final entry: ${entryEntity.date}",
                e
            )
            return null
        }
    }
}
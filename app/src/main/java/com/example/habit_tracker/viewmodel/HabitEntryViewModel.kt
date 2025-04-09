package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitEntryEntity
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.toUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class HabitEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val entryDao: HabitEntryDao = AppDatabase.getDatabase(application).habitEntryDao()
    private val habitDao: HabitDao = AppDatabase.getDatabase(application).habitDao()

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    val entriesForCurrentMonth: StateFlow<List<HabitEntry>> =
        entryDao.getAllEntries()
            .combine(habitDao.getAllHabits()) { entries, habits ->
                Pair(entries, habits)
            }
            .combine(_currentYearMonth) { (entryEntities, allHabits), yearMonth ->
                entryEntities
                    .filter { entryEntity ->
                        try {
                            YearMonth.from(LocalDate.parse(entryEntity.date)) == yearMonth
                        } catch (e: Exception) {
                            Log.e(
                                "HabitEntryViewModel",
                                "Could not parse date: ${entryEntity.date}",
                                e
                            )
                            false
                        }
                    }
                    .mapNotNull { filteredEntity ->
                        filteredEntity.toDomainModel(allHabits)
                    }
                    .sortedByDescending { it.date }
            }
            .stateIn(
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

    fun getEntriesWithHabits(allHabits: List<HabitEntity>): Flow<List<HabitEntry>> {
        return entryDao.getAllEntries()
            .map { list ->
                list.mapNotNull { it.toDomainModel(allHabits) }
            }
    }

    fun saveEntry(entry: HabitEntryEntity) {
        viewModelScope.launch {
            entryDao.insertOrUpdate(entry)
        }
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            entryDao.deleteByDate(date.toString())
        }
    }

    suspend fun getEntryByDate(date: LocalDate): HabitEntry? {
        val allHabits = habitDao.getAllHabits().first()
        return entryDao.getAllEntries().firstOrNull()
            ?.mapNotNull { it.toDomainModel(allHabits) }
            ?.find { it.date == date }
    }
}


fun HabitEntryEntity.toDomainModel(allHabits: List<HabitEntity>): HabitEntry? {
    return try {
        val mappedHabits = habitIds.mapNotNull { id ->
            allHabits.find { it.id == id }?.toUiModel()
        }.map { habit ->
            HabitProgress(habit = habit)
        }

        HabitEntry(
            date = LocalDate.parse(this.date),
            mood = Mood.valueOf(this.mood),
            // notes = this.notes, <-- Removed this line
            habits = mappedHabits
        )
    } catch (e: Exception) {
        Log.e("MappingError", "Failed to map HabitEntryEntity (Date: ${this.date}): ${e.message}")
        null
    }
}
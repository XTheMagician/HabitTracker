package com.example.habit_tracker.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitDao
import com.example.habit_tracker.data.db.HabitEntryDao
import com.example.habit_tracker.data.db.HabitProgressDao
import com.example.habit_tracker.data.db.HabitProgressEntity
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.toEntity
import com.example.habit_tracker.model.toUiModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HabitEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val db =
        AppDatabase.getDatabase(application /*, viewModelScope */) // Pass scope if needed by DB init
    private val entryDao: HabitEntryDao = db.habitEntryDao()
    private val habitDao: HabitDao = db.habitDao()
    private val progressDao: HabitProgressDao = db.habitProgressDao() // Get instance of new DAO

    private val _currentYearMonth = MutableStateFlow(YearMonth.now())
    val currentYearMonth: StateFlow<YearMonth> = _currentYearMonth.asStateFlow()

    // Flow combining entries, progress, and habits to create domain HabitEntry objects
    val entriesForCurrentMonth: StateFlow<List<HabitEntry>> = combine(
        entryDao.getAllEntries(), // Flow<List<HabitEntryEntity>>
        habitDao.getAllHabits(),  // Flow<List<HabitEntity>> - fetch all habits once
        _currentYearMonth
        // Note: Fetching ALL progress might be inefficient for many entries.
        // A more complex query could fetch progress only for the visible month.
        // For simplicity now, we fetch all progress associated with fetched entries.
    ) { entryEntities, allHabitEntities, yearMonth ->
        // Filter entries by the current month first
        val filteredEntryEntities = entryEntities.filter { entryEntity ->
            try {
                YearMonth.from(LocalDate.parse(entryEntity.date)) == yearMonth
            } catch (e: Exception) {
                Log.e(
                    "HabitEntryViewModel",
                    "Filter - Could not parse date: ${entryEntity.date}",
                    e
                )
                false
            }
        }

        // Create a map of HabitEntity for quick lookup by ID
        val habitsMap = allHabitEntities.associateBy { it.id }

        // Fetch progress specifically for the filtered entries
        val resultEntries = mutableListOf<HabitEntry>()
        for (entryEntity in filteredEntryEntities) {
            // Fetch progress for this specific entry date (this is a suspend call inside map)
            val progressEntities =
                progressDao.getProgressForDateOnce(entryEntity.date) // Need a suspend fun version

            val habitProgressList = progressEntities.mapNotNull { progressEntity ->
                // Find the corresponding HabitEntity using the ID
                habitsMap[progressEntity.habitId]?.let { habitEntity ->
                    HabitProgress(
                        habit = habitEntity.toUiModel(), // Convert entity to domain Habit
                        value = progressEntity.value      // Get the stored value
                    )
                }
            }

            try {
                resultEntries.add(
                    HabitEntry(
                        date = LocalDate.parse(entryEntity.date),
                        mood = Mood.valueOf(entryEntity.mood),
                        habits = habitProgressList // Assign the constructed list
                    )
                )
            } catch (e: Exception) {
                Log.e("HabitEntryViewModel", "Map - Could not parse entry: ${entryEntity.date}", e)
            }
        }
        resultEntries.sortedByDescending { it.date } // Sort final list

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

    // This function might be less useful now or needs similar logic to entriesForCurrentMonth
    // Deprecating or removing might be best unless you have a specific use case.
    /*
    fun getEntriesWithHabits(allHabits: List<HabitEntity>): Flow<List<HabitEntry>> {
        // Needs complete rewrite based on new structure
        // Placeholder: return empty flow or implement similar logic as above
        return flowOf(emptyList())
    }
    */

    // --- MODIFIED saveEntry ---
    // Takes the domain HabitEntry object now
    fun saveEntry(entry: HabitEntry) {
        viewModelScope.launch {
            // 1. Save the basic entry info (date, mood)
            entryDao.insertOrUpdate(entry.toEntity()) // Uses updated HabitEntry.toEntity()

            // 2. Delete ALL existing progress for this date to ensure clean state
            val dateString = entry.date.format(DateTimeFormatter.ISO_DATE)
            progressDao.deleteAllProgressForDate(dateString)

            // 3. Insert new progress records for the habits included in the entry
            entry.habits.forEach { habitProgress ->
                val progressEntity = HabitProgressEntity(
                    entryDate = dateString,
                    habitId = habitProgress.habit.id,
                    value = habitProgress.value // Save the value (null or Int)
                )
                progressDao.insertOrUpdateProgress(progressEntity)
            }
        }
    }
    // --- END MODIFIED saveEntry ---

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            // Deleting the entry should cascade and delete progress due to ForeignKey constraint
            entryDao.deleteByDate(date.format(DateTimeFormatter.ISO_DATE))
        }
    }

    // --- MODIFIED getEntryByDate ---
    // Needs to fetch progress and habits similarly to the main flow
    suspend fun getEntryByDate(date: LocalDate): HabitEntry? {
        val dateString = date.format(DateTimeFormatter.ISO_DATE)
        val entryEntity =
            entryDao.getEntryByDateOnce(dateString) ?: return null // Need getEntryByDateOnce
        val progressEntities = progressDao.getProgressForDateOnce(dateString)
            ?: return null // Need getProgressForDateOnce

        if (progressEntities.isEmpty()) {
            // Handle entry with no habits logged - return entry with empty list
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
        val habitsMap =
            habitDao.getHabitsByIdsOnce(habitIds).associateBy { it.id } // Use suspend version

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
    // --- END MODIFIED getEntryByDate ---

}

// Remove the old HabitEntryEntity.toDomainModel extension function - it's obsolete
// fun HabitEntryEntity.toDomainModel(...) { ... }
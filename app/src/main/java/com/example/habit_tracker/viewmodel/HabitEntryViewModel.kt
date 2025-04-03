package com.example.habit_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.data.db.HabitEntryEntity
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.toUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate

class HabitEntryViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).habitEntryDao()

    fun getEntriesWithHabits(allHabits: List<HabitEntity>): Flow<List<HabitEntry>> {
        return dao.getAllEntries()
            .map { list ->
                list.mapNotNull { it.toDomainModel(allHabits) }
            }
    }

    fun saveEntry(entry: HabitEntryEntity) {
        viewModelScope.launch {
            dao.insertOrUpdate(entry)
        }
    }

    fun deleteEntry(date: LocalDate) {
        viewModelScope.launch {
            dao.deleteByDate(date.toString())
        }
    }

    suspend fun getEntryByDate(date: LocalDate): HabitEntry? {
        return getEntriesWithHabits(
            AppDatabase.getDatabase(getApplication()).habitDao().getAllHabits().first()
        ).firstOrNull()?.find { it.date == date }
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
            habits = mappedHabits
        )
    } catch (e: Exception) {
        null
    }
}

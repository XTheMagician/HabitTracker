package com.example.habit_tracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.habit_tracker.data.db.AppDatabase
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.model.toUiModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HabitViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).habitDao()

    val habits = dao.getAllHabits()
        .map { list -> list.map { it.toUiModel() } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val groupedHabits = habits
        .map { habitList -> habitList.groupBy { it.category } }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyMap())

    fun addHabit(habit: HabitEntity) {
        viewModelScope.launch {
            dao.insert(habit)
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            dao.delete(habit)
        }
    }

    fun clearAllHabits() {
        viewModelScope.launch {
            dao.deleteAll()
        }
    }
}

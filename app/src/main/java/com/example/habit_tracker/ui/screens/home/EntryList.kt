package com.example.habit_tracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.toEntity
import com.example.habit_tracker.ui.components.EntryItem
import com.example.habit_tracker.viewmodel.HabitEntryViewModel
import com.example.habit_tracker.viewmodel.HabitViewModel

@Composable
fun EntryList(
    navController: NavController,
    habitEntryViewModel: HabitEntryViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel()
) {
    val allHabits by habitViewModel.habits.collectAsState()
    val entries by habitEntryViewModel
        .getEntriesWithHabits(allHabits.map { it.toEntity() })
        .collectAsState(initial = emptyList())

    fun onDelete(entry: HabitEntry) {
        habitEntryViewModel.deleteEntry(entry.date)
    }

    fun onEdit(entry: HabitEntry) {
        val moodString = entry.mood.name
        val dateString = entry.date.toString()
        navController.navigate("addEntry/$moodString/$dateString")
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp) // for FAB spacing if needed
    ) {
        items(entries) { entry ->
            EntryItem(
                entry = entry,
                onDelete = { onDelete(entry) },
                onEdit = { onEdit(entry) }
            )
        }
    }
}

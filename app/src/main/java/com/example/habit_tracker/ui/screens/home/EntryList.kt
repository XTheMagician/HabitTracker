package com.example.habit_tracker.ui.screens.home
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items 
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.ui.components.EntryItem


@Composable
fun EntryList(
    entries: List<HabitEntry>,
    onDelete: (HabitEntry) -> Unit,
    onEdit: (HabitEntry) -> Unit,
    modifier: Modifier = Modifier
) {

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(items = entries, key = { entry -> entry.date }) { entry ->
            EntryItem(
                entry = entry,
                onDelete = { onDelete(entry) },
                onEdit = { onEdit(entry) }
            )
        }
    }
}
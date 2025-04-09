package com.example.habit_tracker.ui.screens.home // Or ui.components if you moved it

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items // Ensure this specific import is present
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.ui.components.EntryItem // Make sure EntryItem is imported

// Removed NavController and ViewModel imports

@Composable
fun EntryList(
    entries: List<HabitEntry>,          // <-- Takes the list directly
    onDelete: (HabitEntry) -> Unit,     // <-- Callback for delete action
    onEdit: (HabitEntry) -> Unit,       // <-- Callback for edit action
    modifier: Modifier = Modifier       // <-- Standard modifier parameter
) {
    // Removed: ViewModel instances and data fetching logic

    LazyColumn(
        modifier = modifier.fillMaxSize(), // Use the passed modifier
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Use the passed 'entries' list. Add a key for performance/stability.
        items(items = entries, key = { entry -> entry.date }) { entry ->
            EntryItem(
                entry = entry,
                // Pass the callbacks directly to EntryItem
                onDelete = { onDelete(entry) },
                onEdit = { onEdit(entry) }
            )
        }
    }
}
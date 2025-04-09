package com.example.habit_tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitType
import com.example.habit_tracker.model.getIconForMood
import com.example.habit_tracker.model.getLabelForMood
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import java.time.format.DateTimeFormatter


private fun getScaleLevelDisplayString(value: Int?): String? {
    return when (value) {
        1 -> "Low"
        2 -> "Med"
        3 -> "High"
        else -> null
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EntryItem(
    entry: HabitEntry,
    onEdit: (HabitEntry) -> Unit,
    onDelete: (HabitEntry) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val dateText = entry.date.format(DateTimeFormatter.ofPattern("dd. MMM yyyy"))

    val context = LocalContext.current
    LaunchedEffect(Unit) { MaterialSymbolsRepository.preload(context) }

    Card(
        modifier = Modifier.fillMaxWidth(), // Card will take full width
        // Height will be determined by content unless constrained by parent
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Column wraps content

            Row( // Top row for Date/Mood/Menu
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column( // Column for Date/Mood
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = dateText, style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = getIconForMood(entry.mood),
                            contentDescription = "Mood Icon",
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getLabelForMood(entry.mood),
                            fontSize = 16.sp
                        )
                    }
                }

                Box { // Box for Menu Button/Dropdown
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                expanded = false
                                onEdit(entry)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                expanded = false
                                onDelete(entry)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp)) // Space between top row and habits

            // Habits display using FlowRow
            if (entry.habits.isNotEmpty()) { // Conditionally display FlowRow if habits exist
                FlowRow(
                    modifier = Modifier.fillMaxWidth(), // Allow FlowRow to use available width
                    // Height will depend on how many rows it needs to wrap
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.habits.forEach { habitProgress ->
                        val habit = habitProgress.habit
                        val value = habitProgress.value

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val symbolChar =
                                MaterialSymbolsRepository.getSymbolCharSafe(habit.iconKey)

                            Text(
                                text = symbolChar,
                                fontFamily = MaterialSymbols,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(end = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            var displayText = habit.name
                            if (habit.type == HabitType.SCALE) {
                                val levelString = getScaleLevelDisplayString(value)
                                if (levelString != null) {
                                    displayText += " ($levelString)"
                                }
                            }
                            Text(displayText, fontSize = 14.sp)
                        }
                    }
                } // End FlowRow
            } // End if habits not empty
        } // End Main Column inside Card
    } // End Card
}
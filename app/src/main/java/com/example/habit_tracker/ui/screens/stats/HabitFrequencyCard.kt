package com.example.habit_tracker.ui.components // Adjust package if needed

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
// Remove Modifier.size import if not used elsewhere
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
// Optional: Add LaunchedEffect and LocalContext if preload is needed
// import androidx.compose.runtime.LaunchedEffect
// import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Import sp for font size
import androidx.lifecycle.compose.collectAsStateWithLifecycle
// --- Import necessary items for the icon display ---
import com.example.habit_tracker.ui.theme.MaterialSymbols // Your custom font family
import com.example.habit_tracker.utils.MaterialSymbolsRepository // Your repository
// --- End Icon Display Imports ---
import com.example.habit_tracker.viewmodel.StatisticsViewModel

@Composable
fun HabitFrequencyCard(
    viewModel: StatisticsViewModel
) {
    val frequencyData by viewModel.habitFrequencyData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isHabitFrequencyLoading.collectAsStateWithLifecycle()
    val showFrequencyList = !isLoading && frequencyData.isNotEmpty()
    val showEmptyMessage = !isLoading && frequencyData.isEmpty()

    // Optional: Preload symbols if this card might appear before EntryItem
    // val context = LocalContext.current
    // LaunchedEffect(Unit) { MaterialSymbolsRepository.preload(context) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Habit Completions (Last 30 Days)", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    showFrequencyList -> {
                        Column(horizontalAlignment = Alignment.Start) {
                            frequencyData.forEach { stat -> // stat is HabitFrequencyStat
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // --- Use MaterialSymbolsRepository ---
                                    val symbolChar =
                                        MaterialSymbolsRepository.getSymbolCharSafe(stat.iconName) // Get character

                                    Text( // Display character using Text and custom font
                                        text = symbolChar,
                                        fontFamily = MaterialSymbols, // Apply the special font
                                        fontSize = 18.sp, // Match font size from EntryItem or adjust
                                        modifier = Modifier.padding(end = 4.dp), // Padding after icon text
                                        color = MaterialTheme.colorScheme.onSurfaceVariant // Optional: Match color
                                    )
                                    // --- End Icon Display ---

                                    // Habit Name
                                    Text(
                                        text = stat.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f) // Takes up remaining space
                                    )
                                    Spacer(modifier = Modifier.width(8.dp)) // Space before count

                                    // Completion Count (Number only)
                                    Text(
                                        text = "${stat.count}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp)) // Space between rows
                            }
                        }
                    }

                    showEmptyMessage -> {
                        Text("No habits completed in the last 30 days.")
                    }
                }
            }
        }
    }
}
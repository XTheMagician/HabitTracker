package com.example.habit_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import com.example.habit_tracker.viewmodel.HabitFrequencyStat
import com.example.habit_tracker.viewmodel.StatisticsViewModel

@Composable
fun HabitFrequencyCard(
    viewModel: StatisticsViewModel
) { 
    val frequencyData by viewModel.habitFrequencyData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isHabitFrequencyLoading.collectAsStateWithLifecycle()
    val showFrequencyList = !isLoading && frequencyData.isNotEmpty()
    val showEmptyMessage = !isLoading && frequencyData.isEmpty()

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
            Text("Habit Completions", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> CircularProgressIndicator()
                    showFrequencyList -> FrequencyList(frequencyData)
                    showEmptyMessage -> Text("No habits completed in the last 30 days.")
                }
            }
        }
    }
}

@Composable
private fun FrequencyList(
    frequencyData: List<HabitFrequencyStat>,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        frequencyData.forEach { stat ->
            FrequencyItem(stat)
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun FrequencyItem(
    stat: HabitFrequencyStat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(stat.iconName)

        Text(
            text = symbolChar,
            fontFamily = MaterialSymbols,
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 4.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = stat.name,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "${stat.count}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
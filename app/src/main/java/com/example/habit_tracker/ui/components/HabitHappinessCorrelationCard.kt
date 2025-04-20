package com.example.habit_tracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import com.example.habit_tracker.viewmodel.HabitCorrelationResult
import com.example.habit_tracker.viewmodel.StatisticsViewModel
import kotlin.math.abs

@Composable
fun HabitMoodCorrelationListCard(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val correlationResults by viewModel.allCorrelationResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAllCorrelationsLoading.collectAsStateWithLifecycle()

    val showList = !isLoading && correlationResults.isNotEmpty()
    val showEmptyMessage = !isLoading && correlationResults.isEmpty()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text("Habit/Mood Correlation", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .defaultMinSize(minHeight = 50.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }
                    showList -> {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            correlationResults.forEach { result ->
                                CorrelationItemRow(result = result)
                            }
                        }
                    }
                    showEmptyMessage -> {
                        Text(
                            "No significant correlations calculable for this period.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CorrelationItemRow(
    result: HabitCorrelationResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(result.habitIconName)
            Text(
                text = symbolChar,
                fontFamily = MaterialSymbols,
                fontSize = 18.sp,
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = result.habitName,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 8.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = formatCoefficient(result.coefficient),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getCorrelationItemColor(result.coefficient, result.pValue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = formatPValue(result.pValue),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (result.pValue != null && result.pValue < 0.05) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatCoefficient(coefficient: Double?): String {
    return coefficient?.let { coeff ->
        val formatted = "%.2f".format(coeff)
        if (coeff >= 0.0) "+$formatted" else formatted
    } ?: "N/A"
}

private fun formatPValue(pValue: Double?): String {
    return pValue?.let { p -> 
        if (p < 0.001) "p < 0.001" else "p = %.3f".format(p)
    } ?: "(p N/A)"
}

private fun getCorrelationItemColor(rho: Double?, pValue: Double?): Color {
    val alpha = 0.05
    val isSignificant = pValue != null && pValue < alpha

    return when {
        rho == null || !rho.isFinite() -> Color.Gray
        isSignificant && abs(rho) >= 0.7 -> if (rho > 0) Color(0xFF1B5E20) else Color(0xFFB71C1C)
        isSignificant && abs(rho) >= 0.4 -> if (rho > 0) Color(0xFF388E3C) else Color(0xFFD32F2F)
        isSignificant && abs(rho) >= 0.1 -> if (rho > 0) Color(0xFF66BB6A) else Color(0xFFEF5350)
        else -> Color.DarkGray
    }
}

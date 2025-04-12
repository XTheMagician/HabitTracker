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
    // Collect state from ViewModel
    val correlationResults by viewModel.allCorrelationResults.collectAsStateWithLifecycle()
    val isLoading by viewModel.isAllCorrelationsLoading.collectAsStateWithLifecycle()

    // Determine states for display
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
                    .defaultMinSize(minHeight = 50.dp), // Give some min height
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
                            // Display only top N results? Or all? Displaying all for now.
                            correlationResults.forEach { result ->
                                CorrelationItemRow(result = result) // Use helper composable
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

// Helper composable for displaying a single correlation result row
@Composable
private fun CorrelationItemRow( // Renamed for clarity
    result: HabitCorrelationResult,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left side: Icon and Name
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
                modifier = Modifier.padding(end = 8.dp) // Space before coefficient
            )
        }

        // Right side: Coefficient and P-Value
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = result.coefficient?.let { coeff -> // Give the non-null coefficient a name
                    val formatted = "%.2f".format(coeff) // Format it
                    if (coeff >= 0.0) "+$formatted" else formatted // Prepend "+" if original coeff >= 0.0
                } ?: "N/A", // Fallback if coefficient is null
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getCorrelationItemColor(
                    result.coefficient,
                    result.pValue
                ) // Updated color logic
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = result.pValue?.let { p -> if (p < 0.001) "p < 0.001" else "p = %.3f".format(p) }
                    ?: "(p N/A)",
                style = MaterialTheme.typography.bodySmall,
                // Conditionally make p-value bold if significant
                fontWeight = if (result.pValue != null && result.pValue < 0.05) FontWeight.Bold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}


// Updated helper to optionally factor in p-value for color
private fun getCorrelationItemColor(rho: Double?, pValue: Double?): Color {
    // Significance threshold (e.g., 0.05)
    val alpha = 0.05
    val isSignificant = pValue != null && pValue < alpha

    return when {
        rho == null || !rho.isFinite() -> Color.Gray // Error state
        // Only color significantly if p-value is low enough
        isSignificant && abs(rho) >= 0.7 -> if (rho > 0) Color(0xFF1B5E20) else Color(0xFFB71C1C) // Stronger Green/Red
        isSignificant && abs(rho) >= 0.4 -> if (rho > 0) Color(0xFF388E3C) else Color(0xFFD32F2F) // Moderate Green/Red
        isSignificant && abs(rho) >= 0.1 -> if (rho > 0) Color(0xFF66BB6A) else Color(0xFFEF5350) // Weaker Green/Red
        // If not significant OR weak correlation even if significant by chance, use muted color
        else -> Color.DarkGray
    }
}

// Import abs if not already done at file level

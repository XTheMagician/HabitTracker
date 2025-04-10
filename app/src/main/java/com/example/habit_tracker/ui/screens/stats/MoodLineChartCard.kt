package com.example.habit_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.habit_tracker.data.EntryRepository.clear
import com.example.habit_tracker.viewmodel.StatisticsViewModel
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@Composable
fun MoodLineChartCard( // Renamed the function
    viewModel: StatisticsViewModel
) {
    // Collect the chart data and state flags from your ViewModel (SAME AS MoodChartCard)
    val chartData by viewModel.moodChartData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle() // Using the original isLoading state
    val showChart by viewModel.showMoodChart.collectAsStateWithLifecycle()

    // Create the model producer (SAME AS MoodChartCard)
    val modelProducer = remember { CartesianChartModelProducer() }

    // Update the chart whenever the data changes.
    LaunchedEffect(chartData) {
        // Use runTransaction (SAME AS MoodChartCard)
        modelProducer.runTransaction {
            if (chartData.isNotEmpty()) {
                // --- CHANGE: Use lineSeries like the example ---
                // The example `series(13, 8, ...)` passes only Y-values.
                // We do the same by mapping our chartData pairs to their second element (mood value).
                lineSeries {
                    series(chartData.map { it.second })
                }
                // --- End CHANGE ---
            } else {
                // Use clear() from the transaction scope (SAME AS MoodChartCard)
                clear() // Correct way to call clear within the scope
            }
        }
    }

    // Display the chart inside a Card (SAME STRUCTURE AS MoodChartCard).
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Slightly different title for clarity
            Text("Mood Trend (Line)", style = MaterialTheme.typography.titleMedium)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator()
                    }

                    showChart -> {
                        CartesianChartHost(
                            // --- CHANGE: Adapt the rememberCartesianChart call ---
                            chart = rememberCartesianChart(
                                // 1. Use the LINE layer
                                layers = arrayOf(rememberLineCartesianLayer()),
                                // 2. Use the same axis configuration as your working example
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom()
                            ),
                            // --- End CHANGE ---
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        // Slightly different text
                        Text("Not enough mood data for trend line (need >= 2 points).")
                    }
                }
            }
        }
    }
}
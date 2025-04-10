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
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

@Composable
fun MoodChartCard(
    viewModel: StatisticsViewModel
) {
    // Collect the chart data and state flags from your ViewModel.
    val chartData by viewModel.moodChartData.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val showChart by viewModel.showMoodChart.collectAsStateWithLifecycle()

    // Create the model producer.
    val modelProducer = remember { CartesianChartModelProducer() }

    // Update the chart whenever the data changes.
    LaunchedEffect(chartData) {
        modelProducer.runTransaction {
            if (chartData.isNotEmpty()) {
                // Map the mood values (Y-values) and pass them as a Collection<Number>.
                columnSeries {
                    series(chartData.map { it.second })
                }
            } else {
                clear()
            }
        }
    }

    // Display the chart inside a Card.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Mood Last 30 Days", style = MaterialTheme.typography.titleMedium)
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
                            // Use the updated parameter "layers" with a list of a column layer.
                            chart = rememberCartesianChart(
                                layers = arrayOf(rememberColumnCartesianLayer()),
                                startAxis = VerticalAxis.rememberStart(),
                                bottomAxis = HorizontalAxis.rememberBottom()
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        Text("Not enough mood data recorded recently.")
                    }
                }
            }
        }
    }
}

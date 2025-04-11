package com.example.habit_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.data.EntryRepository.clear
import com.example.habit_tracker.model.Mood
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries

// Removed formatter/math imports as they are not used now
// import com.patrykandpatrick.vico.core.cartesian.formatter.CartesianValueFormatter
// import kotlin.math.roundToInt

@Composable
fun MoodDistributionChartCard(
    title: String,
    moodDistribution: Map<Mood, Int>?,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    // moodOrder is only needed if we customize labels later, keep for now
    val moodOrder = remember {
        listOf(Mood.VERY_BAD, Mood.BAD, Mood.NEUTRAL, Mood.GOOD, Mood.VERY_GOOD)
    }
    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(moodDistribution) {
        modelProducer.runTransaction {
            clear()
            if (!moodDistribution.isNullOrEmpty()) {
                val counts = moodOrder.map { mood ->
                    moodDistribution.getOrDefault(mood, 0).toFloat()
                }
                columnSeries {
                    series(counts) // Provide the Y-values (counts)
                }
            }
        }
    }

    // Removed valueFormatter declarations

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
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

                    !moodDistribution.isNullOrEmpty() -> {
                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                layers = arrayOf(rememberColumnCartesianLayer()),
                                // Use default axes without formatters or item placers
                                startAxis = VerticalAxis.rememberStart(), // Vico default labels and ticks
                                bottomAxis = HorizontalAxis.rememberBottom() // Vico default labels and ticks
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    else -> {
                        Text("No mood data recorded for this period.")
                    }
                }
            }
        }
    }
}
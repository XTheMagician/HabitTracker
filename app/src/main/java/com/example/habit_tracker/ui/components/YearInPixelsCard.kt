package com.example.habit_tracker.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.habit_tracker.model.Mood
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// --- Color definitions (Keep these or move to Theme/Constants) ---
val VeryBadColor = Color(0xFFD32F2F)
val BadColor = Color(0xFFFFA000)
val NeutralColor = Color(0xFF1976D2)
val GoodColor = Color(0xFF388E3C)
val VeryGoodColor = Color(0xFFCDDC39)
val NoEntryColor = Color.LightGray.copy(alpha = 0.5f)

fun getColorForMood(mood: Mood?): Color {
    return when (mood) {
        Mood.VERY_BAD -> VeryBadColor
        Mood.BAD -> BadColor
        Mood.NEUTRAL -> NeutralColor
        Mood.GOOD -> GoodColor
        Mood.VERY_GOOD -> VeryGoodColor
        null -> NoEntryColor
    }
}
// --- End Colors ---


@OptIn(ExperimentalLayoutApi::class) // Needed for FlowRow
@Composable
fun YearInPixelsCard(
    selectedYear: Year,
    pixelData: Map<LocalDate, Mood>, // Map of Date -> Mood
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Year in Pixels (${selectedYear.value})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    // Display months vertically
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Space between month rows
                    ) {
                        // Iterate through all 12 months
                        Month.entries.forEach { month ->
                            MonthPixelRow(
                                yearMonth = YearMonth.of(selectedYear.value, month),
                                pixelData = pixelData
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class) // Needed for FlowRow
@Composable
private fun MonthPixelRow(
    yearMonth: YearMonth,
    pixelData: Map<LocalDate, Mood>,
    modifier: Modifier = Modifier
) {
    val monthDisplayName = remember(yearMonth) {
        yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)

    val pixelSize = 10.dp // Smaller pixel size might fit better
    val pixelSpacing = 2.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month Label (fixed width)
        Text(
            text = monthDisplayName.take(3), // Abbreviate month name (e.g., "Jan")
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp) // Give label some space
        )
        Spacer(modifier = Modifier.width(8.dp))

        // Use FlowRow to wrap pixels automatically
        FlowRow(
            modifier = Modifier.weight(1f), // Take remaining space
            horizontalArrangement = Arrangement.spacedBy(pixelSpacing),
            verticalArrangement = Arrangement.spacedBy(pixelSpacing)
        ) {
            // Generate pixels for each day of the month
            for (day in 1..daysInMonth) {
                val date = yearMonth.atDay(day)
                val mood = pixelData[date] // Look up mood for this specific date

                Box(
                    modifier = Modifier
                        .size(pixelSize)
                        .clip(CircleShape) // Make them circular dots
                        .background(getColorForMood(mood))
                )
            }
        }
    }
}
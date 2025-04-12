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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import com.example.habit_tracker.viewmodel.HabitCompletionStatus
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

val HabitNotDoneColor = Color(0xFFF44336)   // Example: Red
val HabitNoEntryColor = Color.LightGray.copy(alpha = 0.5f)

val HabitDoneBinaryColor = Color(0xFF4CAF50)   // Example: Green (for binary)
val HabitDoneScaleLowColor = Color(0xFFFFA000)  // Example: Orange (for scale 1) - Adjust as needed
val HabitDoneScaleMediumColor = Color(0xFFCDDC39) // Example: Lime (for scale 2) - Adjust as needed
val HabitDoneScaleHighColor = Color(0xFF388E3C)

fun getColorForHabitStatus(status: HabitCompletionStatus?): Color {
    return when (status) {
        HabitCompletionStatus.DONE_BINARY -> HabitDoneBinaryColor
        HabitCompletionStatus.DONE_SCALE_LOW -> HabitDoneScaleLowColor
        HabitCompletionStatus.DONE_SCALE_MEDIUM -> HabitDoneScaleMediumColor
        HabitCompletionStatus.DONE_SCALE_HIGH -> HabitDoneScaleHighColor
        HabitCompletionStatus.NOT_DONE -> HabitNotDoneColor
        HabitCompletionStatus.NO_ENTRY -> HabitNoEntryColor
        null -> HabitNoEntryColor // Default fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitYearInPixelsCard(
    selectedYear: Year,
    allHabits: List<HabitEntity>, // List of available habits
    selectedHabitId: Int?,        // Currently selected habit ID
    habitPixelData: Map<LocalDate, HabitCompletionStatus>, // Pixel data for the selected habit
    isLoading: Boolean,           // Loading state for pixel data
    onHabitSelected: (Int?) -> Unit, // Callback when a habit is selected
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
    // Find the selected habit name for display, default to "Select Habit"
    val selectedHabitText = remember(selectedHabitId, allHabits) {
        allHabits.find { it.id == selectedHabitId }?.name ?: "Select a Habit"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Habit Year in Pixels (${selectedYear.value})",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))

            // --- Habit Selector Dropdown ---
            ExposedDropdownMenuBox(
                expanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = !isDropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField( // Or use standard TextField if preferred
                    value = selectedHabitText,
                    onValueChange = {}, // Input is read-only
                    readOnly = true,
                    label = { Text("Habit") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor() // Important for positioning the dropdown
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false }
                ) {
                    // Option to deselect
                    DropdownMenuItem(
                        text = { Text("None") },
                        onClick = {
                            onHabitSelected(null) // Pass null ID
                            isDropdownExpanded = false
                        }
                    )
                    // Options for each habit
                    allHabits.forEach { habit ->
                        DropdownMenuItem(
                            text = { Text(habit.name) },
                            leadingIcon = {
                                // Use the same icon rendering technique
                                val symbolChar =
                                    MaterialSymbolsRepository.getSymbolCharSafe(habit.iconName)
                                Text(
                                    text = symbolChar,
                                    fontFamily = MaterialSymbols, // Apply the special font
                                    fontSize = 18.sp // Adjust size as needed for menu items
                                    // Color will typically be inherited from DropdownMenuItem's content color
                                )
                            },
                            onClick = {
                                onHabitSelected(habit.id) // Pass selected habit's ID
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            // --- End Dropdown ---

            Spacer(Modifier.height(16.dp))

            // --- Pixel Grid Area ---
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading && selectedHabitId != null) { // Only show loading if a habit is selected and loading
                    CircularProgressIndicator()
                } else if (selectedHabitId == null) {
                    Text("Please select a habit above.")
                } else {
                    // Reuse the pixel grid layout logic (can be extracted later)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Month.entries.forEach { month ->
                            HabitMonthPixelRow( // Use a distinct name for clarity
                                yearMonth = YearMonth.of(selectedYear.value, month),
                                pixelData = habitPixelData // Pass the habit-specific pixel data
                            )
                        }
                    }
                }
            }
        }
    }
}

// Reusable composable for the row of pixels for a specific month
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitMonthPixelRow( // Changed name slightly
    yearMonth: YearMonth,
    pixelData: Map<LocalDate, HabitCompletionStatus>, // Expects habit status data
    modifier: Modifier = Modifier
) {
    val monthDisplayName = remember(yearMonth) {
        yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
    }
    val daysInMonth = yearMonth.lengthOfMonth()
    val pixelSize = 10.dp
    val pixelSpacing = 2.dp

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthDisplayName.take(3),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(40.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))

        FlowRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(pixelSpacing),
            verticalArrangement = Arrangement.spacedBy(pixelSpacing)
        ) {
            for (day in 1..daysInMonth) {
                val date = yearMonth.atDay(day)
                // Look up habit status for this specific date
                val status =
                    pixelData[date] ?: HabitCompletionStatus.NO_ENTRY // Default if somehow missing

                Box(
                    modifier = Modifier
                        .size(pixelSize)
                        .clip(CircleShape)
                        // Use the habit status color function
                        .background(getColorForHabitStatus(status))
                )
            }
        }
    }
}
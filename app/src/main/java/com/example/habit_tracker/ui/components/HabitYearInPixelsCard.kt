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

val HabitNotDoneColor = Color(0xFFF44336)
val HabitNoEntryColor = Color.LightGray.copy(alpha = 0.5f)

val HabitDoneBinaryColor = Color(0xFF4CAF50)  
val HabitDoneScaleLowColor = Color(0xFFFFA000)
val HabitDoneScaleMediumColor = Color(0xFFCDDC39)
val HabitDoneScaleHighColor = Color(0xFF388E3C)

fun getColorForHabitStatus(status: HabitCompletionStatus?): Color {
    return when (status) {
        HabitCompletionStatus.DONE_BINARY -> HabitDoneBinaryColor
        HabitCompletionStatus.DONE_SCALE_LOW -> HabitDoneScaleLowColor
        HabitCompletionStatus.DONE_SCALE_MEDIUM -> HabitDoneScaleMediumColor
        HabitCompletionStatus.DONE_SCALE_HIGH -> HabitDoneScaleHighColor
        HabitCompletionStatus.NOT_DONE -> HabitNotDoneColor
        HabitCompletionStatus.NO_ENTRY -> HabitNoEntryColor
        null -> HabitNoEntryColor
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HabitYearInPixelsCard(
    selectedYear: Year,
    allHabits: List<HabitEntity>,
    selectedHabitId: Int?,
    habitPixelData: Map<LocalDate, HabitCompletionStatus>,
    isLoading: Boolean,
    onHabitSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }
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

            HabitSelector(
                selectedHabitText = selectedHabitText,
                isDropdownExpanded = isDropdownExpanded,
                onExpandedChange = { isDropdownExpanded = it },
                allHabits = allHabits,
                onHabitSelected = onHabitSelected
            )

            Spacer(Modifier.height(16.dp))

            PixelGridArea(
                selectedHabitId = selectedHabitId,
                selectedYear = selectedYear,
                habitPixelData = habitPixelData,
                isLoading = isLoading
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitSelector(
    selectedHabitText: String,
    isDropdownExpanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    allHabits: List<HabitEntity>,
    onHabitSelected: (Int?) -> Unit,
    modifier: Modifier = Modifier
) {
    ExposedDropdownMenuBox(
        expanded = isDropdownExpanded,
        onExpandedChange = { onExpandedChange(!isDropdownExpanded) },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedHabitText,
            onValueChange = {},
            readOnly = true,
            label = { Text("Habit") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = isDropdownExpanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            DropdownMenuItem(
                text = { Text("None") },
                onClick = {
                    onHabitSelected(null)
                    onExpandedChange(false)
                }
            )
            
            allHabits.forEach { habit ->
                DropdownMenuItem(
                    text = { Text(habit.name) },
                    leadingIcon = {
                        val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(habit.iconName)
                        Text(
                            text = symbolChar,
                            fontFamily = MaterialSymbols,
                            fontSize = 18.sp
                        )
                    },
                    onClick = {
                        onHabitSelected(habit.id)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}

@Composable
private fun PixelGridArea(
    selectedHabitId: Int?,
    selectedYear: Year,
    habitPixelData: Map<LocalDate, HabitCompletionStatus>,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading && selectedHabitId != null -> CircularProgressIndicator()
            selectedHabitId == null -> Text("Please select a habit above.")
            else -> MonthlyPixelGrid(selectedYear, habitPixelData)
        }
    }
}

@Composable
private fun MonthlyPixelGrid(
    selectedYear: Year,
    habitPixelData: Map<LocalDate, HabitCompletionStatus>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Month.entries.forEach { month ->
            HabitMonthPixelRow(
                yearMonth = YearMonth.of(selectedYear.value, month),
                pixelData = habitPixelData
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitMonthPixelRow(
    yearMonth: YearMonth,
    pixelData: Map<LocalDate, HabitCompletionStatus>,
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
                val status = pixelData[date] ?: HabitCompletionStatus.NO_ENTRY

                Box(
                    modifier = Modifier
                        .size(pixelSize)
                        .clip(CircleShape)
                        .background(getColorForHabitStatus(status))
                )
            }
        }
    }
}
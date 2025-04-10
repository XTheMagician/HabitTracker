package com.example.habit_tracker.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.ui.components.BottomNavigationBar
import com.example.habit_tracker.viewmodel.HabitEntryViewModel
import com.example.habit_tracker.viewmodel.HabitViewModel
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    navController: NavController,
    habitEntryViewModel: HabitEntryViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel()
) {

    val currentYearMonth by habitEntryViewModel.currentYearMonth.collectAsStateWithLifecycle()
    val entries by habitEntryViewModel.entriesForCurrentMonth.collectAsStateWithLifecycle()

    val monthYearFormatter = remember(Locale.getDefault()) {
        DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault())
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navController.navigate("addEntry")
            }) {
                Text("+")
            }
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            MonthSwitcher(
                currentMonth = currentYearMonth,
                monthYearFormatter = monthYearFormatter,
                onPreviousMonth = habitEntryViewModel::showPreviousMonth,
                onNextMonth = habitEntryViewModel::showNextMonth,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            )

            EntryList(
                entries = entries, // Pass the state variable holding the filtered list
                onDelete = { entryToDelete -> // Define the delete action here
                    habitEntryViewModel.deleteEntry(entryToDelete.date)
                },
                onEdit = { entryToEdit -> // Define the edit action here
                    val moodString = entryToEdit.mood.name
                    val dateString = entryToEdit.date.toString()
                    // Use the navController available in HomeScreen to navigate
                    navController.navigate("habitSelection/$moodString/$dateString")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // Use weight modifier here if needed
                    .padding(horizontal = 16.dp) // Apply padding as needed
            )
        }
    }
}

@Composable
fun MonthSwitcher(
    currentMonth: YearMonth,
    monthYearFormatter: DateTimeFormatter,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Previous Month"
            )
        }

        Text(
            text = currentMonth.format(monthYearFormatter)
                // Capitalize the first letter of the month
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            style = MaterialTheme.typography.headlineSmall, // Or another appropriate style
            textAlign = TextAlign.Center
        )

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Next Month"
            )
        }
    }
}
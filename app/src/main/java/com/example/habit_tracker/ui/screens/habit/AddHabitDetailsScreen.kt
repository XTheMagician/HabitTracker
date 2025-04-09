package com.example.habit_tracker.ui.screens.habit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.habit_tracker.model.HabitType
import com.example.habit_tracker.ui.navigation.AppDestinations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDetailsScreen(
    navController: NavController,
    category: String
) {
    var habitName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(HabitType.BINARY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Habit Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (habitName.isNotBlank()) {
                        navController.navigate(
                            AppDestinations.buildSelectHabitIconRoute(
                                category = category,
                                name = habitName,
                                type = selectedType
                            )
                        )
                    }
                }
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next: Select Icon"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Adding habit to category: $category",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = habitName,
                onValueChange = { habitName = it },
                label = { Text("Habit Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Habit Type:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                val types = HabitType.values()

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    types.forEachIndexed { index, habitType ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = types.size
                            ),
                            onClick = { selectedType = habitType },
                            selected = (habitType == selectedType)
                        ) {
                            Text(habitType.name)
                        }
                    }
                }
            }
        }
    }
}
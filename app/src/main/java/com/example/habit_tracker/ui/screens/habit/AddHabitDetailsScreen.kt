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

    val isFormValid = habitName.isNotBlank()

    Scaffold(
        topBar = {
            HabitDetailsTopBar(
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            NextButton(
                onClick = {
                    if (isFormValid) {
                        navigateToIconSelection(navController, category, habitName, selectedType)
                    }
                },
                enabled = isFormValid
            )
        }
    ) { paddingValues ->
        HabitDetailsContent(
            category = category,
            habitName = habitName,
            onHabitNameChange = { habitName = it },
            selectedType = selectedType,
            onTypeSelect = { selectedType = it },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        )
    }
}

@Composable
private fun HabitDetailsContent(
    category: String,
    habitName: String,
    onHabitNameChange: (String) -> Unit,
    selectedType: HabitType,
    onTypeSelect: (HabitType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = "Adding habit to category: $category",
            style = MaterialTheme.typography.titleMedium
        )

        HabitNameField(
            value = habitName,
            onValueChange = onHabitNameChange,
            modifier = Modifier.fillMaxWidth()
        )

        HabitTypeSelector(
            selectedType = selectedType,
            onTypeSelect = onTypeSelect,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HabitNameField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Habit Name") },
        modifier = modifier,
        singleLine = true
    )
}

@Composable
private fun HabitTypeSelector(
    selectedType: HabitType,
    onTypeSelect: (HabitType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
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
                    onClick = { onTypeSelect(habitType) },
                    selected = (habitType == selectedType)
                ) {
                    Text(habitType.name)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitDetailsTopBar(
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = { Text("Add New Habit Details") },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun NextButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Next: Select Icon"
        )
    }
}

private fun navigateToIconSelection(
    navController: NavController,
    category: String,
    habitName: String,
    selectedType: HabitType
) {
    navController.navigate(
        AppDestinations.buildSelectHabitIconRoute(
            category = category,
            name = habitName,
            type = selectedType
        )
    )
}
package com.example.habit_tracker.ui.screens.habit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.model.Habit
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.getIconForMood
import com.example.habit_tracker.model.getLabelForMood
import com.example.habit_tracker.model.toEntity
import com.example.habit_tracker.ui.navigation.AppDestinations
import com.example.habit_tracker.ui.screens.mood.MoodOption
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import com.example.habit_tracker.viewmodel.HabitEntryViewModel
import com.example.habit_tracker.viewmodel.HabitViewModel
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HabitSelectionScreen(
    navController: NavController,
    mood: Mood,
    date: LocalDate,
    entryViewModel: HabitEntryViewModel = viewModel(),
    habitViewModel: HabitViewModel = viewModel()
) {
    val selectedHabits = remember { mutableStateMapOf<Int, Boolean>() }
    val habits = habitViewModel.habits.collectAsState().value
    var selectedMood by remember { mutableStateOf(mood) }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    // --- NEW: State for New Category Dialog ---
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryNameInput by remember { mutableStateOf("") }
    // --- END NEW STATE ---

    LaunchedEffect(habits) {
        if (habits.isNotEmpty()) {
            val allHabitEntities = habits.mapNotNull { it?.toEntity() }
            if (allHabitEntities.isNotEmpty()) {
                val matchingEntry = entryViewModel
                    .getEntriesWithHabits(allHabitEntities)
                    .firstOrNull()
                    ?.find { it.date == date }

                matchingEntry?.habits?.forEach { habitProgress ->
                    selectedHabits[habitProgress.habit.id] = true
                }
            }
        }
    }

    fun save() {
        val selected = habits.filter { selectedHabits[it.id] == true }
        val entry = HabitEntry(
            date = date,
            mood = selectedMood,
            habits = selected.map { habit -> HabitProgress(habit) }
        )
        entryViewModel.saveEntry(entry.toEntity())
        navController.navigate(AppDestinations.HOME) {
            popUpTo(AppDestinations.HOME) { inclusive = true }
            launchSingleTop = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    TextButton(onClick = { save() }) {
                        Text("Speichern")
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 120.dp), // Ensure padding for FAB + new button
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text(
                    text = "How was your day?",
                    style = MaterialTheme.typography.headlineSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Mood.values().reversed().forEach { moodItem ->
                        MoodOption(
                            icon = getIconForMood(moodItem),
                            label = getLabelForMood(moodItem),
                            mood = moodItem,
                            selectedMood = selectedMood
                        ) {
                            selectedMood = it
                        }
                    }
                }

                habits.groupBy { it.category }.forEach { (category, habitGroup) ->
                    HabitGroupCard(
                        title = category ?: "General",
                        onAddClick = {
                            val categoryNameToPass = category ?: "General"
                            navController.navigate(
                                AppDestinations.buildAddHabitDetailsRoute(
                                    categoryNameToPass
                                )
                            )
                        }) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            habitGroup.forEach { habit ->
                                HabitButton(
                                    habit = habit,
                                    selected = selectedHabits[habit.id] == true,
                                    onClick = {
                                        selectedHabits[habit.id] =
                                            !(selectedHabits[habit.id] ?: false)
                                    },
                                    onLongClick = {
                                        habitToDelete = habit
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                } // End groupBy

                // --- NEW: Button to Add New Category ---
                Button( // Or TextButton for less emphasis
                    onClick = {
                        newCategoryNameInput = "" // Clear previous input
                        showNewCategoryDialog = true // Show the dialog
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally) // Center the button
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Add New Category")
                }
                // --- END NEW BUTTON ---

            } // End Main Column

            Box( // FAB Box
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),

                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(onClick = { save() }) {
                    Icon(Icons.Default.Check, contentDescription = "Speichern")
                }
            }

            // --- Delete Dialog --- (Keep existing dialog logic)
            if (showDeleteDialog && habitToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteDialog = false
                        habitToDelete = null
                    },
                    title = { Text("Delete Habit") },
                    text = { Text("Are you sure you want to delete '${habitToDelete?.name}'?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                habitToDelete?.let {
                                    habitViewModel.deleteHabit(it.toEntity())
                                }
                                showDeleteDialog = false
                                habitToDelete = null
                            }
                        ) {
                            Text("Delete", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteDialog = false
                                habitToDelete = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            } // End Delete Dialog

            // --- NEW: New Category Dialog --- (Implementation)
            if (showNewCategoryDialog) {
                // Define state specifically for the dialog's error handling
                var categoryNameError by remember { mutableStateOf<String?>(null) }
                val existingCategories = remember(habits) {
                    habits.mapNotNull { it.category?.lowercase() }.toSet()
                }

                AlertDialog(
                    onDismissRequest = {
                        // User clicked outside or pressed back
                        showNewCategoryDialog = false
                        newCategoryNameInput = "" // Clear input on dismiss
                        categoryNameError = null // Clear error
                    },
                    title = { Text("New Habit Category") },
                    text = {
                        // Column to hold TextField and potential error message
                        Column {
                            OutlinedTextField(
                                value = newCategoryNameInput,
                                onValueChange = {
                                    newCategoryNameInput = it
                                    // Clear error when user types
                                    if (categoryNameError != null) {
                                        categoryNameError = null
                                    }
                                },
                                label = { Text("Category Name") },
                                singleLine = true,
                                isError = categoryNameError != null // Show error state if message exists
                            )
                            // Display error message if present
                            if (categoryNameError != null) {
                                Text(
                                    text = categoryNameError!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val trimmedName = newCategoryNameInput.trim()
                                // --- Validation ---
                                if (trimmedName.isBlank()) {
                                    categoryNameError = "Category name cannot be empty."
                                } else if (existingCategories.contains(trimmedName.lowercase())) {
                                    categoryNameError = "'$trimmedName' already exists."
                                } else {
                                    // --- Validation Passed ---
                                    categoryNameError = null // Clear any previous error
                                    showNewCategoryDialog = false // Close dialog
                                    // Navigate to add habit details for the new category
                                    navController.navigate(
                                        AppDestinations.buildAddHabitDetailsRoute(trimmedName)
                                    )
                                    newCategoryNameInput = "" // Clear input after navigation
                                }
                            },
                            // Disable button if input is blank initially
                            enabled = newCategoryNameInput.isNotBlank()
                        ) {
                            Text("Create & Add Habit")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showNewCategoryDialog = false
                                newCategoryNameInput = "" // Clear input
                                categoryNameError = null // Clear error
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }
            // --- END NEW CATEGORY DIALOG ---

        } // End Outer Box
    } // End Scaffold
}

@Composable
fun HabitGroupCard(
    title: String,
    onAddClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onAddClick) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HabitButton(
    habit: Habit,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { MaterialSymbolsRepository.preload(context) }

    val bgColor =
        if (selected) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.3f)
    val contentColor =
        if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = bgColor,
            modifier = Modifier.size(56.dp),
            tonalElevation = if (selected) 2.dp else 1.dp,
            shadowElevation = if (selected) 4.dp else 2.dp
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(
                    habit.iconKey,
                    fallbackSymbolName = "question_mark"
                )
                Text(
                    text = symbolChar,
                    fontFamily = MaterialSymbols,
                    fontSize = 32.sp,
                    color = contentColor
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(habit.name, fontSize = 12.sp)
    }
}
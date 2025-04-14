package com.example.habit_tracker.ui.screens.habit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.model.Habit
import com.example.habit_tracker.model.HabitEntry
import com.example.habit_tracker.model.HabitProgress
import com.example.habit_tracker.model.HabitType
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
    val habitProgressMap = remember { mutableStateMapOf<Int, Int?>() }
    val habits = habitViewModel.habits.collectAsState().value
    var selectedMood by remember { mutableStateOf(mood) }
    val scrollState = rememberScrollState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryNameInput by remember { mutableStateOf("") }

    var showScalableInputDialog by remember { mutableStateOf(false) }
    var scalableHabitToEdit by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(date) {
        habitProgressMap.clear()
        val existingEntry: HabitEntry? = entryViewModel.getEntryByDate(date)
        if (existingEntry != null) {
            existingEntry.habits.forEach { habitProgress ->
                habitProgressMap[habitProgress.habit.id] = habitProgress.value
            }
        }
    }

    fun save() {
        val progressList = mutableListOf<HabitProgress>()
        habitProgressMap.forEach { (habitId, value) ->
            val habit = habits.find { it.id == habitId }
            if (habit != null && value != null) {
                progressList.add(HabitProgress(habit = habit, value = value))
            }
        }

        val entry = HabitEntry(
            date = date,
            mood = selectedMood,
            habits = progressList
        )
        entryViewModel.saveEntry(entry)

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
                    .padding(bottom = 120.dp),
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
                                    selected = habitProgressMap[habit.id] != null,
                                    onClick = {
                                        if (habit.type == HabitType.BINARY) {
                                            habitProgressMap[habit.id] =
                                                if (habitProgressMap[habit.id] == null) 1 else null
                                        } else {
                                            scalableHabitToEdit = habit
                                            showScalableInputDialog = true
                                        }
                                    },
                                    onLongClick = {
                                        habitToDelete = habit
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        newCategoryNameInput = ""
                        showNewCategoryDialog = true
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Add New Category")
                }

            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingActionButton(onClick = { save() }) {
                    Icon(Icons.Default.Check, contentDescription = "Speichern")
                }
            }


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
                                    habitProgressMap.remove(it.id)
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
            }


            if (showNewCategoryDialog) {
                var categoryNameError by remember { mutableStateOf<String?>(null) }
                val existingCategories = remember(habits) {
                    habits.mapNotNull { it.category?.lowercase() }.toSet()
                }

                AlertDialog(
                    onDismissRequest = {
                        showNewCategoryDialog = false
                        newCategoryNameInput = ""
                        categoryNameError = null
                    },
                    title = { Text("New Habit Category") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newCategoryNameInput,
                                onValueChange = {
                                    newCategoryNameInput = it
                                    if (categoryNameError != null) {
                                        categoryNameError = null
                                    }
                                },
                                label = { Text("Category Name") },
                                singleLine = true,
                                isError = categoryNameError != null
                            )
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
                                if (trimmedName.isBlank()) {
                                    categoryNameError = "Category name cannot be empty."
                                } else if (existingCategories.contains(trimmedName.lowercase())) {
                                    categoryNameError = "'$trimmedName' already exists."
                                } else {
                                    categoryNameError = null
                                    showNewCategoryDialog = false
                                    navController.navigate(
                                        AppDestinations.buildAddHabitDetailsRoute(trimmedName)
                                    )
                                    newCategoryNameInput = ""
                                }
                            },
                            enabled = newCategoryNameInput.isNotBlank()
                        ) {
                            Text("Create & Add Habit")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showNewCategoryDialog = false
                                newCategoryNameInput = ""
                                categoryNameError = null
                            }
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showScalableInputDialog && scalableHabitToEdit != null) {
                val scaleOptions =
                    remember { listOf(null to "None", 1 to "Low", 2 to "Med", 3 to "High") }
                val currentLevel = habitProgressMap[scalableHabitToEdit?.id]

                AlertDialog(
                    onDismissRequest = {
                        showScalableInputDialog = false
                        scalableHabitToEdit = null
                    },
                    // Provide content in the TRAILING LAMBDA
                    content = {
                        SingleChoiceSegmentedButtonRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp) // Add some padding
                        ) {
                            scaleOptions.forEachIndexed { index, (levelValue, levelLabel) ->
                                SegmentedButton(
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = scaleOptions.size
                                    ),
                                    onClick = {
                                        scalableHabitToEdit?.let {
                                            habitProgressMap[it.id] = levelValue
                                        }
                                        showScalableInputDialog = false
                                        scalableHabitToEdit = null
                                    },
                                    selected = (levelValue == currentLevel)
                                ) {
                                    Text(levelLabel)
                                }
                            }
                        }
                    } // End of content lambda
                ) // End AlertDialog call
            }


        }
    }
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

    // Define colors for selected/unselected states
    val selectedContainerColor = MaterialTheme.colorScheme.primary
    // Use a subtle background for unselected state for better visual grouping if desired
    val unselectedContainerColor =
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f) // Or Color.Transparent
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
    val unselectedContentColor =
        MaterialTheme.colorScheme.onSurfaceVariant // Color for the icon itself

    // Apply combinedClickable to the Column, making the whole unit interactive
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            // Make the column itself detect clicks and long clicks
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            // Add padding *around* the clickable area if needed,
            // or inside the column before the elements for spacing
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        // Use a Box to control the background, shape, and size of the icon area
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp) // Visual size of the button background
                .background(
                    color = if (selected) selectedContainerColor else unselectedContainerColor,
                    // Use a shape consistent with IconButton, e.g., CircleShape or rounded corner
                    shape = MaterialTheme.shapes.extraLarge // Often works well for ~56dp icons
                )
        ) {
            // Icon Text - color is now set directly based on selection state
            val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(
                habit.iconKey,
                fallbackSymbolName = "question_mark"
            )
            Text(
                text = symbolChar,
                fontFamily = MaterialSymbols,
                fontSize = 32.sp, // Adjust if needed
                color = if (selected) selectedContentColor else unselectedContentColor // Explicitly set icon color
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = habit.name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface // Use a standard text color
            // Optional: Change text color based on selection too
            // color = if (selected) MaterialTheme.colorScheme.primary else LocalContentColor.current
        )
    }
}
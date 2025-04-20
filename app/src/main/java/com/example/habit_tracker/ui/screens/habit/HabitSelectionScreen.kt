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

    var showDeleteDialog by remember { mutableStateOf(false) }
    var habitToDelete by remember { mutableStateOf<Habit?>(null) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showScalableInputDialog by remember { mutableStateOf(false) }
    var scalableHabitToEdit by remember { mutableStateOf<Habit?>(null) }

    LaunchedEffect(date) {
        loadExistingEntryData(date, entryViewModel, habitProgressMap)
    }

    fun saveEntry() {
        val entry = createHabitEntry(date, selectedMood, habitProgressMap, habits)
        entryViewModel.saveEntry(entry)
        navigateToHome(navController)
    }

    Scaffold(
        topBar = {
            HabitSelectionTopBar(
                onBackClick = { navController.popBackStack() },
                onSaveClick = { saveEntry() }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HabitSelectionContent(
                selectedMood = selectedMood,
                onMoodSelected = { selectedMood = it },
                habits = habits,
                habitProgressMap = habitProgressMap,
                onHabitClick = { habit ->
                    handleHabitClick(
                        habit,
                        habitProgressMap,
                        { scalableHabitToEdit = it },
                        { showScalableInputDialog = it }
                    )
                },
                onHabitLongClick = { habit ->
                    habitToDelete = habit
                    showDeleteDialog = true
                },
                onAddCategoryClick = {
                    showNewCategoryDialog = true
                },
                onAddHabitToCategory = { category ->
                    navController.navigate(AppDestinations.buildAddHabitDetailsRoute(category))
                }
            )

            SaveButton(
                onClick = { saveEntry() },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 24.dp),
            )

            if (showDeleteDialog && habitToDelete != null) {
                DeleteHabitDialog(
                    habit = habitToDelete!!,
                    onDismiss = {
                        showDeleteDialog = false
                        habitToDelete = null
                    },
                    onConfirm = {
                        habitToDelete?.let {
                            habitProgressMap.remove(it.id)
                            habitViewModel.deleteHabit(it.toEntity())
                        }
                        showDeleteDialog = false
                        habitToDelete = null
                    }
                )
            }

            if (showNewCategoryDialog) {
                NewCategoryDialog(
                    habits = habits,
                    onDismiss = { showNewCategoryDialog = false },
                    onConfirm = { categoryName ->
                        showNewCategoryDialog = false
                        navController.navigate(
                            AppDestinations.buildAddHabitDetailsRoute(categoryName)
                        )
                    }
                )
            }

            if (showScalableInputDialog && scalableHabitToEdit != null) {
                ScalableHabitDialog(
                    habit = scalableHabitToEdit!!,
                    currentLevel = habitProgressMap[scalableHabitToEdit?.id],
                    onDismiss = {
                        showScalableInputDialog = false
                        scalableHabitToEdit = null
                    },
                    onLevelSelect = { levelValue ->
                        scalableHabitToEdit?.let {
                            habitProgressMap[it.id] = levelValue
                        }
                        showScalableInputDialog = false
                        scalableHabitToEdit = null
                    }
                )
            }
        }
    }
}

@Composable
private fun HabitSelectionContent(
    selectedMood: Mood,
    onMoodSelected: (Mood) -> Unit,
    habits: List<Habit>,
    habitProgressMap: Map<Int, Int?>,
    onHabitClick: (Habit) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    onAddCategoryClick: () -> Unit,
    onAddHabitToCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
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

        MoodSelectionRow(
            selectedMood = selectedMood,
            onMoodSelected = onMoodSelected
        )

        HabitCategoriesSection(
            habits = habits,
            habitProgressMap = habitProgressMap,
            onHabitClick = onHabitClick,
            onHabitLongClick = onHabitLongClick,
            onAddHabitToCategory = onAddHabitToCategory
        )

        Spacer(modifier = Modifier.height(24.dp))
        AddCategoryButton(
            onClick = onAddCategoryClick,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun MoodSelectionRow(
    selectedMood: Mood,
    onMoodSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Mood.values().reversed().forEach { moodItem ->
            MoodOption(
                icon = getIconForMood(moodItem),
                label = getLabelForMood(moodItem),
                mood = moodItem,
                selectedMood = selectedMood
            ) {
                onMoodSelected(it)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitCategoriesSection(
    habits: List<Habit>,
    habitProgressMap: Map<Int, Int?>,
    onHabitClick: (Habit) -> Unit,
    onHabitLongClick: (Habit) -> Unit,
    onAddHabitToCategory: (String) -> Unit
) {
    habits.groupBy { it.category }.forEach { (category, habitGroup) ->
        HabitGroupCard(
            title = category ?: "General",
            onAddClick = {
                val categoryNameToPass = category ?: "General"
                onAddHabitToCategory(categoryNameToPass)
            }
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                habitGroup.forEach { habit ->
                    HabitButton(
                        habit = habit,
                        selected = habitProgressMap[habit.id] != null,
                        onClick = { onHabitClick(habit) },
                        onLongClick = { onHabitLongClick(habit) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCategoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
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

@Composable
private fun SaveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        FloatingActionButton(onClick = onClick) {
            Icon(Icons.Default.Check, contentDescription = "Save")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HabitSelectionTopBar(
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(onClick = onSaveClick) {
                Text("Save")
            }
        }
    )
}

@Composable
private fun DeleteHabitDialog(
    habit: Habit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Habit") },
        text = { Text("Are you sure you want to delete '${habit.name}'?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Delete", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun NewCategoryDialog(
    habits: List<Habit>,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newCategoryNameInput by remember { mutableStateOf("") }
    var categoryNameError by remember { mutableStateOf<String?>(null) }

    val existingCategories = remember(habits) {
        habits.mapNotNull { it.category?.lowercase() }.toSet()
    }

    AlertDialog(
        onDismissRequest = {
            onDismiss()
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
                        onConfirm(trimmedName)
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
                    onDismiss()
                    newCategoryNameInput = ""
                    categoryNameError = null
                }
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScalableHabitDialog(
    habit: Habit,
    currentLevel: Int?,
    onDismiss: () -> Unit,
    onLevelSelect: (Int?) -> Unit
) {
    val scaleOptions = remember { listOf(null to "None", 1 to "Low", 2 to "Med", 3 to "High") }

    AlertDialog(
        onDismissRequest = onDismiss,
        content = {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                scaleOptions.forEachIndexed { index, (levelValue, levelLabel) ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = scaleOptions.size
                        ),
                        onClick = { onLevelSelect(levelValue) },
                        selected = (levelValue == currentLevel)
                    ) {
                        Text(levelLabel)
                    }
                }
            }
        }
    )
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

    val selectedContainerColor = MaterialTheme.colorScheme.primary
    val unselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
    val unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(vertical = 4.dp, horizontal = 4.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(56.dp)
                .background(
                    color = if (selected) selectedContainerColor else unselectedContainerColor,
                    shape = MaterialTheme.shapes.extraLarge
                )
        ) {
            val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(
                habit.iconKey,
                fallbackSymbolName = "question_mark"
            )
            Text(
                text = symbolChar,
                fontFamily = MaterialSymbols,
                fontSize = 32.sp,
                color = if (selected) selectedContentColor else unselectedContentColor
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = habit.name,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private suspend fun loadExistingEntryData(
    date: LocalDate,
    entryViewModel: HabitEntryViewModel,
    habitProgressMap: MutableMap<Int, Int?>
) {
    habitProgressMap.clear()
    val existingEntry: HabitEntry? = entryViewModel.getEntryByDate(date)
    existingEntry?.habits?.forEach { habitProgress ->
        habitProgressMap[habitProgress.habit.id] = habitProgress.value
    }
}

private fun createHabitEntry(
    date: LocalDate,
    mood: Mood,
    habitProgressMap: Map<Int, Int?>,
    habits: List<Habit>
): HabitEntry {
    val progressList = mutableListOf<HabitProgress>()
    habitProgressMap.forEach { (habitId, value) ->
        val habit = habits.find { it.id == habitId }
        if (habit != null && value != null) {
            progressList.add(HabitProgress(habit = habit, value = value))
        }
    }

    return HabitEntry(
        date = date,
        mood = mood,
        habits = progressList
    )
}

private fun navigateToHome(navController: NavController) {
    navController.navigate(AppDestinations.HOME) {
        popUpTo(AppDestinations.HOME) { inclusive = true }
        launchSingleTop = true
    }
}

private fun handleHabitClick(
    habit: Habit,
    habitProgressMap: MutableMap<Int, Int?>,
    setHabitToEdit: (Habit?) -> Unit,
    showDialog: (Boolean) -> Unit
) {
    if (habit.type == HabitType.BINARY) {
        habitProgressMap[habit.id] = if (habitProgressMap[habit.id] == null) 1 else null
    } else {
        setHabitToEdit(habit)
        showDialog(true)
    }
}
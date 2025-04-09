package com.example.habit_tracker.ui.screens.habit // Adjust package if needed

// *** Import the Repository and Font ***
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.habit_tracker.data.db.HabitEntity
import com.example.habit_tracker.model.HabitType
import com.example.habit_tracker.ui.navigation.AppDestinations
import com.example.habit_tracker.ui.theme.MaterialSymbols
import com.example.habit_tracker.utils.MaterialSymbolsRepository
import com.example.habit_tracker.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectHabitIconScreen(
    navController: NavController,
    habitViewModel: HabitViewModel,
    category: String,
    habitName: String,
    habitType: HabitType
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // State for the search query
    var searchQuery by remember { mutableStateOf("") }
    // State for the selected icon's name (String key)
    var selectedIconName by remember { mutableStateOf<String?>(null) }
    // State to hold the loaded codepoints map
    var codepoints by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    // State to track if loading is complete (optional, for showing a loading indicator)
    var isLoading by remember { mutableStateOf(true) }

    // Load codepoints when the screen is first composed
    LaunchedEffect(key1 = Unit) { // key1 = Unit ensures it runs only once
        isLoading = true
        codepoints = MaterialSymbolsRepository.getCodepoints(context)
        isLoading = false
    }

    // Filter the icon *names* based on the search query
    val filteredIconNames = remember(searchQuery, codepoints) {
        if (searchQuery.isBlank()) {
            codepoints.keys.sorted().toList() // Show all names, sorted alphabetically
        } else {
            codepoints.keys.filter { name ->
                name.contains(searchQuery, ignoreCase = true)
            }.sorted().toList()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Icon") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            // Show FAB only if an icon is selected AND codepoints are loaded
            if (selectedIconName != null && !isLoading) {
                FloatingActionButton(
                    onClick = {
                        // Create the HabitEntity and save it
                        val newHabit = HabitEntity(
                            name = habitName,
                            iconName = selectedIconName!!, // Save the NAME
                            type = habitType,
                            category = category
                        )
                        habitViewModel.addHabit(newHabit)

                        // Pop back stack to the previous screen (HabitSelection)
                        navController.popBackStack(
                            route = AppDestinations.HABIT_SELECTION_ROUTE,
                            inclusive = false
                        )
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = "Save Habit")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                label = { Text("Search Symbols") }, // Updated label
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                },
                singleLine = true
            )

            // Loading Indicator (Optional)
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Grid of Icons
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 72.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredIconNames, key = { it }) { iconName ->
                        // Get the character representation for the symbol name
                        // Use the safe version with a fallback
                        val symbolChar = MaterialSymbolsRepository.getSymbolCharSafe(iconName)

                        SymbolGridItem( // Changed to SymbolGridItem
                            symbolName = iconName,
                            symbolChar = symbolChar, // Pass the character string
                            isSelected = iconName == selectedIconName,
                            onClick = {
                                selectedIconName = iconName // Update selected state
                            }
                        )
                    }
                }
            } // End else (not loading)
        } // End Column
    } // End Scaffold
}

// Composable for a single item in the symbol grid (Modified from IconGridItem)
@Composable
fun SymbolGridItem(
    symbolName: String,
    symbolChar: String, // Changed from iconVector
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
    val backgroundColor =
        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
    val contentColor =
        if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .border(2.dp, borderColor, CircleShape)
            .background(backgroundColor, CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // *** Use Text composable with the MaterialSymbols font ***
        Text(
            text = symbolChar, // Display the character(s) for the symbol
            fontFamily = MaterialSymbols, // Apply the custom font
            fontSize = 36.sp, // Adjust size as needed
            color = contentColor
            // Optional: Adjust weight, grade etc. if needed using fontVariationSettings
            // fontWeight = FontWeight.Normal // Example
        )
    }
}
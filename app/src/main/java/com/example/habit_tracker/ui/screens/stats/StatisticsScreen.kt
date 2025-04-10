package com.example.habit_tracker.ui.screens.stats // Or .screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer // Import Spacer
import androidx.compose.foundation.layout.height // Import height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState // Import if needed
import androidx.compose.foundation.verticalScroll // Import if needed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.habit_tracker.ui.components.BottomNavigationBar // Import your BottomNavBar
import com.example.habit_tracker.ui.components.MoodChartCard // Your original working column chart
import com.example.habit_tracker.ui.components.MoodLineChartCard // Import the NEW line chart card
import com.example.habit_tracker.viewmodel.StatisticsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    navController: NavController, // For navigation if needed
    statisticsViewModel: StatisticsViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Statistics") })
            // Add navigation icon if needed
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp) // Apply horizontal padding once
                .verticalScroll(rememberScrollState()) // Add scrolling
        ) {
            // Original Column Chart
            MoodChartCard(viewModel = statisticsViewModel)


            // New Line Chart
            MoodLineChartCard(viewModel = statisticsViewModel) // Add the new card

            Spacer(modifier = Modifier.height(16.dp)) // Optional space at the bottom
        }
    }
}
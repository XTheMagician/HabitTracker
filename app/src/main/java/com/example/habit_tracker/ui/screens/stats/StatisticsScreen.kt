package com.example.habit_tracker.ui.screens.stats // Or .screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
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
import com.example.habit_tracker.ui.components.MoodChartCard
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
                .padding(16.dp) // Add padding for content
        ) {
            Text("Statistics Screen Content (Chart Card will go here)")
            MoodChartCard(viewModel = viewModel())
        }
    }
}
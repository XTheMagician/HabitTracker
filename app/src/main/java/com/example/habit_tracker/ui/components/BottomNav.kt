package com.example.habit_tracker.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue // Import getValue
import androidx.navigation.NavController // Import NavController
import androidx.navigation.NavDestination.Companion.hierarchy // For hierarchy check
import androidx.navigation.NavGraph.Companion.findStartDestination // For popUpTo logic
import androidx.navigation.compose.currentBackStackEntryAsState // Import currentBackStackEntryAsState
import com.example.habit_tracker.ui.navigation.AppDestinations // Import your destinations object

// Define items for the navigation bar
private val items = listOf(
    ScreenNavItem(
        "Entries",
        AppDestinations.HOME,
        Icons.Default.List
    ), // Assuming HOME is your entries list
    ScreenNavItem("Stats", AppDestinations.STATISTICS, Icons.Default.BarChart),
    // Add Calendar and More destinations if/when you create them
    ScreenNavItem("Calendar", "calendar_route", Icons.Default.CalendarToday), // Placeholder route
    ScreenNavItem("More", "more_route", Icons.Default.MoreHoriz) // Placeholder route
)

// Data class to hold navigation item info
private data class ScreenNavItem(
    val label: String,
    val route: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

@Composable
fun BottomNavigationBar(navController: NavController) { // Accept NavController
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                // Determine if item is selected by comparing current route hierarchy
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    // Navigate only if the selected destination is different
                    if (currentDestination?.route != screen.route) {
                        navController.navigate(screen.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            // on the back stack as users select items
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}
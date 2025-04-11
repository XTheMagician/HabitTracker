package com.example.habit_tracker.ui.components // Or adjust package as needed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import java.time.Year // Import Year class

@Composable
fun YearSwitcher(
    currentYear: Year,          // Takes the currently selected Year
    onPreviousYear: () -> Unit, // Callback when the back arrow is clicked
    onNextYear: () -> Unit,     // Callback when the forward arrow is clicked
    modifier: Modifier = Modifier // Optional modifier for styling/layout
) {
    Row(
        modifier = modifier.fillMaxWidth(), // Occupy full width by default
        verticalAlignment = Alignment.CenterVertically, // Align items vertically centered
        horizontalArrangement = Arrangement.SpaceBetween // Space out the arrows and text
    ) {
        // Button for Previous Year
        IconButton(onClick = onPreviousYear) { // Trigger the callback on click
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack, // Use standard back arrow
                contentDescription = "Previous Year" // Accessibility description
            )
        }

        // Display the Current Year
        Text(
            text = currentYear.toString(), // Convert Year object to String
            style = MaterialTheme.typography.headlineSmall, // Use an appropriate text style
            textAlign = TextAlign.Center // Center the year text
        )

        // Button for Next Year
        IconButton(onClick = onNextYear) { // Trigger the callback on click
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward, // Use standard forward arrow
                contentDescription = "Next Year" // Accessibility description
            )
        }
    }
}
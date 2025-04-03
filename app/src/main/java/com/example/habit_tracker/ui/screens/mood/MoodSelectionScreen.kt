package com.example.habit_tracker.ui.screens.mood

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.habit_tracker.model.Mood
import com.example.habit_tracker.model.getEmojiForMood
import com.example.habit_tracker.model.getLabelForMood
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEntryScreen(navController: NavController) {
    var selectedMood by remember { mutableStateOf<Mood?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val context = LocalContext.current

    val formattedDate = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, dd. MMMM"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Zurück"
                        )
                    }
                },
                actions = {}
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Text(
                text = "Wie geht's dir?",
                style = MaterialTheme.typography.headlineMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                            },
                            selectedDate.year,
                            selectedDate.monthValue - 1,
                            selectedDate.dayOfMonth
                        ).show()
                    }
            ) {
                Icon(Icons.Filled.CalendarToday, contentDescription = "Datum wählen")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = formattedDate, fontSize = 16.sp)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Mood.values().reversed().forEach { mood ->
                    MoodOption(
                        emoji = getEmojiForMood(mood),
                        label = getLabelForMood(mood),
                        mood = mood,
                        selectedMood = selectedMood
                    ) {
                        selectedMood = it
                        navController.navigate("habitSelection/${it.name}/${selectedDate}")
                    }
                }
            }
        }
    }
}

@Composable
fun MoodOption(
    emoji: String,
    label: String,
    mood: Mood,
    selectedMood: Mood?,
    onClick: (Mood) -> Unit
) {
    val isSelected = mood == selectedMood
    val highlightColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick(mood) }
            .padding(4.dp)
    ) {
        Text(
            text = emoji,
            fontSize = if (isSelected) 32.sp else 28.sp
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = highlightColor
        )
    }
}

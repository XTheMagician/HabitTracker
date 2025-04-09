package com.example.habit_tracker.ui.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Forest
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pending
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Manually curated map of Material Icons for selection.
 */
object AppIcons {
    val map: Map<String, ImageVector> = mapOf(
        // Common Actions
        "Add" to Icons.Filled.Add,
        "Edit" to Icons.Filled.Edit,
        "Delete" to Icons.Filled.Delete,
        "Check" to Icons.Filled.Check,
        "Close" to Icons.Filled.Close,
        "Search" to Icons.Filled.Search,
        "Done" to Icons.Filled.Done,
        "Menu" to Icons.Filled.Menu,
        "Settings" to Icons.Filled.Settings,
        "Refresh" to Icons.Filled.Refresh,
        "MoreVert" to Icons.Filled.MoreVert,

        // Navigation & Social
        "Home" to Icons.Filled.Home,
        "Person" to Icons.Filled.Person,
        "Group" to Icons.Filled.Group,
        "Info" to Icons.Filled.Info,
        "Warning" to Icons.Filled.Warning,
        "Error" to Icons.Filled.Error,
        "Favorite" to Icons.Filled.Favorite,
        "Star" to Icons.Filled.Star,
        "Visibility" to Icons.Filled.Visibility,
        "ThumbUp" to Icons.Filled.ThumbUp,
        "Notifications" to Icons.Filled.Notifications,
        "Email" to Icons.Filled.Email,
        "Call" to Icons.Filled.Call,
        "Place" to Icons.Filled.Place,
        "Map" to Icons.Filled.Map,

        // Activities & Hobbies
        "FitnessCenter" to Icons.Filled.FitnessCenter,
        "Pool" to Icons.Filled.Pool,
        "Book" to Icons.Filled.Book,
        "MenuBook" to Icons.AutoMirrored.Filled.MenuBook, // AutoMirrored
        "MusicNote" to Icons.Filled.MusicNote,
        "Headphones" to Icons.Filled.Headphones,
        "Image" to Icons.Filled.Image,
        "PhotoCamera" to Icons.Filled.PhotoCamera,
        "Brush" to Icons.Filled.Brush,
        "Palette" to Icons.Filled.Palette,
        "Build" to Icons.Filled.Build,
        "Code" to Icons.Filled.Code,
        "SportsEsports" to Icons.Filled.SportsEsports, // Example using a different icon name

        // Nature & Time
        "Park" to Icons.Filled.Park,
        "Forest" to Icons.Filled.Forest,
        "Bedtime" to Icons.Filled.Bedtime,
        "WbSunny" to Icons.Filled.WbSunny,
        "Cloud" to Icons.Filled.Cloud,
        "WaterDrop" to Icons.Filled.WaterDrop,
        "Schedule" to Icons.Filled.Schedule,
        "Alarm" to Icons.Filled.Alarm,
        "CalendarMonth" to Icons.Filled.CalendarMonth,

        // Food & Drink
        "LocalCafe" to Icons.Filled.LocalCafe,
        "LocalDining" to Icons.Filled.LocalDining,
        "Cake" to Icons.Filled.Cake,

        // Other
        "SelfImprovement" to Icons.Filled.SelfImprovement,
        "Face" to Icons.Filled.Face,
        "Pets" to Icons.Filled.Pets,
        "Work" to Icons.Filled.Work,
        "ShoppingCart" to Icons.Filled.ShoppingCart,
        "Savings" to Icons.Filled.Savings,

        // AutoMirrored Examples
        "ArrowBack" to Icons.AutoMirrored.Filled.ArrowBack,
        "ArrowForward" to Icons.AutoMirrored.Filled.ArrowForward,
        "Logout" to Icons.AutoMirrored.Filled.Logout,
        "Notes" to Icons.AutoMirrored.Filled.Notes,
        "Send" to Icons.AutoMirrored.Filled.Send,
        "List" to Icons.AutoMirrored.Filled.List,
        "Sort" to Icons.AutoMirrored.Filled.Sort,
        "Help" to Icons.AutoMirrored.Filled.Help
    )

    // Optional: Provide a default icon if lookup fails
    val defaultIcon: ImageVector = Icons.Filled.Pending // Or any other fallback icon
}
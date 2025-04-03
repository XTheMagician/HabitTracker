import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.AirlineSeatFlat
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Piano
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material.icons.filled.SentimentNeutral
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.vector.ImageVector

val iconMap: Map<String, ImageVector> = mapOf(
    // 🔹 Deine bisherigen
    "FitnessCenter" to Icons.Filled.FitnessCenter,
    "MenuBook" to Icons.AutoMirrored.Filled.MenuBook,
    "Bedtime" to Icons.Filled.Bedtime,
    "SelfImprovement" to Icons.Filled.SelfImprovement,
    "Call" to Icons.Filled.Call,
    "Park" to Icons.Filled.Park,
    "Brush" to Icons.Filled.Brush,
    "Edit" to Icons.Filled.Edit,
    "MusicNote" to Icons.Filled.MusicNote,
    "School" to Icons.Filled.School,
    "ShoppingCart" to Icons.Filled.ShoppingCart,
    "Check" to Icons.Filled.Check,
    "Translate" to Icons.Filled.Translate,
    "Article" to Icons.AutoMirrored.Filled.Article,
    "EventNote" to Icons.AutoMirrored.Filled.EventNote,
    "CleaningServices" to Icons.Filled.CleaningServices,
    "Headphones" to Icons.Filled.Headphones,
    "Movie" to Icons.Filled.Movie,
    "LocalDrink" to Icons.Filled.LocalDrink,
    "Piano" to Icons.Filled.Piano,
    "Newspaper" to Icons.Filled.Newspaper,

    // 🔸 5 Moods (Symbolisch, keine echten Emojis)
    "MoodVeryGood" to Icons.Filled.SentimentVerySatisfied,
    "MoodGood" to Icons.Filled.SentimentSatisfied,
    "MoodNeutral" to Icons.Filled.SentimentNeutral,
    "MoodBad" to Icons.Filled.SentimentDissatisfied,
    "MoodVeryBad" to Icons.Filled.SentimentVeryDissatisfied,

    // 🔸 Zusätzliche Icons für Gewohnheiten
    "Breakfast" to Icons.Filled.FreeBreakfast,
    "Lunch" to Icons.Filled.RamenDining,
    "Dinner" to Icons.Filled.DinnerDining,
    "Toothbrush" to Icons.Filled.MedicalServices,
    "Work" to Icons.Filled.Work,
    "Computer" to Icons.Filled.Computer,
    "WalkDog" to Icons.Filled.Pets,
    "Meditation" to Icons.Filled.SelfImprovement,
    "Sun" to Icons.Filled.WbSunny,
    "Alarm" to Icons.Filled.AccessAlarm,
    "Timer" to Icons.Filled.Timer,
    "DrinkCoffee" to Icons.Filled.Coffee,
    "Run" to Icons.AutoMirrored.Filled.DirectionsRun,
    "Nap" to Icons.Filled.AirlineSeatFlat,
    "Reading" to Icons.AutoMirrored.Filled.MenuBook,
    "Phone" to Icons.Filled.Phone,
    "Cleaning" to Icons.Filled.CleaningServices
)

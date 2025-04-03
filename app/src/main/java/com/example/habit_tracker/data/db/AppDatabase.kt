package com.example.habit_tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [HabitEntryEntity::class, HabitEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val context: Context
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = getDatabase(context).habitDao()

                dao.insertAll(
                    // --- Freetime ---
                    HabitEntity(name = "Read", iconName = "Reading", category = "Freetime"),
                    HabitEntity(name = "Paint or Draw", iconName = "Brush", category = "Freetime"),
                    HabitEntity(
                        name = "Play Instrument",
                        iconName = "MusicNote",
                        category = "Freetime"
                    ),
                    HabitEntity(name = "Read News", iconName = "Newspaper", category = "Freetime"),
                    HabitEntity(name = "Go Outside", iconName = "Park", category = "Freetime"),
                    HabitEntity(
                        name = "Listen to Music",
                        iconName = "Headphones",
                        category = "Freetime"
                    ),
                    HabitEntity(name = "Movie Night", iconName = "Movie", category = "Freetime"),
                    HabitEntity(
                        name = "Relax",
                        iconName = "SelfImprovement",
                        category = "Freetime"
                    ),

                    // --- Health ---
                    HabitEntity(name = "Workout", iconName = "FitnessCenter", category = "Health"),
                    HabitEntity(name = "Walk", iconName = "Run", category = "Health"),
                    HabitEntity(name = "Drink Water", iconName = "LocalDrink", category = "Health"),
                    HabitEntity(name = "Sleep Early", iconName = "Bedtime", category = "Health"),
                    HabitEntity(name = "Meditation", iconName = "Meditation", category = "Health"),

                    // --- Mindfulness ---
                    HabitEntity(
                        name = "Write Journal",
                        iconName = "Edit",
                        category = "Mindfulness"
                    ),

                    // --- Work/Productivity ---
                    HabitEntity(name = "Study", iconName = "School", category = "Work"),
                    HabitEntity(name = "Plan Day", iconName = "EventNote", category = "Work"),
                    HabitEntity(
                        name = "Practice Language",
                        iconName = "Translate",
                        category = "Work"
                    ),
                    HabitEntity(name = "Computer Work", iconName = "Computer", category = "Work"),

                    // --- Household ---
                    HabitEntity(name = "Clean Room", iconName = "Cleaning", category = "Household"),
                    HabitEntity(
                        name = "Grocery Shopping",
                        iconName = "ShoppingCart",
                        category = "Household"
                    ),

                    // --- Social ---
                    HabitEntity(name = "Call Family", iconName = "Phone", category = "Social"),
                    HabitEntity(name = "Write Message", iconName = "Edit", category = "Social"),
                    HabitEntity(name = "Meet Friend", iconName = "Group", category = "Social"),

                    // --- Routine ---
                    HabitEntity(
                        name = "Eat Breakfast",
                        iconName = "Breakfast",
                        category = "Routine"
                    ),
                    HabitEntity(name = "Eat Lunch", iconName = "Lunch", category = "Routine"),
                    HabitEntity(name = "Eat Dinner", iconName = "Dinner", category = "Routine"),
                    HabitEntity(
                        name = "Brush Teeth",
                        iconName = "Toothbrush",
                        category = "Routine"
                    ),
                    HabitEntity(
                        name = "Drink Coffee",
                        iconName = "DrinkCoffee",
                        category = "Routine"
                    ),
                    HabitEntity(name = "Wake Up", iconName = "Sun", category = "Routine"),
                    HabitEntity(name = "Go to Sleep", iconName = "Bedtime", category = "Routine"),
                    HabitEntity(name = "Set Alarm", iconName = "Alarm", category = "Routine"),
                    HabitEntity(name = "Power Nap", iconName = "Nap", category = "Routine"),

                    // --- Pets ---
                    HabitEntity(name = "Walk the Dog", iconName = "WalkDog", category = "Pets")
                )
            }
        }
    }
}

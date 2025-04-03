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
                    HabitEntity(name = "Read", iconName = "MenuBook", category = "Freetime"),
                    HabitEntity(name = "Paint or Draw", iconName = "Brush", category = "Freetime"),
                    HabitEntity(
                        name = "Play Instrument",
                        iconName = "MusicNote",
                        category = "Freetime"
                    ),
                    HabitEntity(name = "Read News", iconName = "Article", category = "Freetime"),
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
                    HabitEntity(name = "Walk", iconName = "DirectionsWalk", category = "Health"),
                    HabitEntity(name = "Drink Water", iconName = "LocalDrink", category = "Health"),
                    HabitEntity(name = "Sleep Early", iconName = "Bedtime", category = "Health"),

                    // --- Mindfulness ---
                    HabitEntity(
                        name = "Meditation",
                        iconName = "SelfImprovement",
                        category = "Mindfulness"
                    ),
                    HabitEntity(
                        name = "Write Journal",
                        iconName = "EditNote",
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

                    // --- Household ---
                    HabitEntity(
                        name = "Clean Room",
                        iconName = "CleaningServices",
                        category = "Household"
                    ),
                    HabitEntity(
                        name = "Grocery Shopping",
                        iconName = "ShoppingCart",
                        category = "Household"
                    ),

                    // --- Social ---
                    HabitEntity(name = "Call Family", iconName = "Call", category = "Social"),
                    HabitEntity(name = "Write Message", iconName = "Chat", category = "Social"),
                    HabitEntity(name = "Meet Friend", iconName = "Group", category = "Social")
                )
            }
        }
    }
}

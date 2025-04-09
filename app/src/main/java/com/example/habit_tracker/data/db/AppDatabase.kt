package com.example.habit_tracker.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.habit_tracker.model.HabitType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

object DefaultHabits {
    val list = listOf(
        HabitEntity(
            name = "Read",
            iconName = "menu_book",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Paint or Draw",
            iconName = "palette",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Play Instrument",
            iconName = "music_note",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Read News",
            iconName = "article",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Go Outside",
            iconName = "park",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Listen to Music",
            iconName = "headphones",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Movie Night",
            iconName = "movie",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Relax",
            iconName = "self_improvement",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Workout",
            iconName = "fitness_center",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Walk",
            iconName = "directions_walk",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Drink Water",
            iconName = "water_drop",
            category = "Health",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Sleep Early",
            iconName = "bedtime",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Meditation",
            iconName = "sentiment_calm",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Write Journal",
            iconName = "edit_note",
            category = "Mindfulness",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Study",
            iconName = "school",
            category = "Work",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Plan Day",
            iconName = "event",
            category = "Work",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Practice Language",
            iconName = "translate",
            category = "Work",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Computer Work",
            iconName = "computer",
            category = "Work",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Clean Room",
            iconName = "cleaning_services",
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Grocery Shopping",
            iconName = "shopping_cart",
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Call Family",
            iconName = "call",
            category = "Social",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Write Message",
            iconName = "chat",
            category = "Social",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Meet Friend",
            iconName = "group",
            category = "Social",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Breakfast",
            iconName = "bakery_dining",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Lunch",
            iconName = "lunch_dining",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Dinner",
            iconName = "dinner_dining",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Brush Teeth",
            iconName = "brush",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Drink Coffee",
            iconName = "local_cafe",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Wake Up",
            iconName = "wb_sunny",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Go to Sleep",
            iconName = "bedtime",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Set Alarm",
            iconName = "alarm",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Power Nap",
            iconName = "airline_seat_individual_suite",
            category = "Routine",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Walk the Dog",
            iconName = "pets",
            category = "Pets",
            type = HabitType.BINARY
        )
    )
}

@Database(
    entities = [HabitEntryEntity::class, HabitEntity::class],
    version = 3, // Or your current/reset version
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(applicationScope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    val dao = database.habitDao()
                    dao.insertAll(*DefaultHabits.list.toTypedArray())
                }
            }
        }
    }
}
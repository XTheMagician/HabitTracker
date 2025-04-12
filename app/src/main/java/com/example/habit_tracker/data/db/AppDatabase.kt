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
        // Freetime
        HabitEntity(
            name = "Read",
            iconName = "menu_book", // Or "auto_stories"
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Gaming",
            iconName = "sports_esports", // Or "stadia_controller"
            category = "Freetime",
            type = HabitType.SCALE // Scalable as requested
        ),
        HabitEntity(
            name = "Movie",
            iconName = "movie", // Or "theaters"
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Datenight",
            iconName = "favorite", // Or "restaurant", "local_bar"
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Trip",
            iconName = "explore", // Or "flight_takeoff", "directions_car"
            category = "Freetime",
            type = HabitType.BINARY
        ),

        // Household
        HabitEntity(
            name = "Groceries",
            iconName = "shopping_cart",
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Cook",
            iconName = "soup_kitchen", // Or "restaurant_menu", "outdoor_grill"
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Wash Clothes",
            iconName = "local_laundry_service",
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Clean Flat",
            iconName = "cleaning_services",
            category = "Household",
            type = HabitType.BINARY
        ),

        // Work
        HabitEntity(
            name = "Study",
            iconName = "school", // Or "menu_book"
            category = "Work",
            type = HabitType.SCALE // Scalable as requested
        ),
        HabitEntity(
            name = "Take a Break",
            iconName = "free_breakfast", // Or "self_improvement", "coffee"
            category = "Work",
            type = HabitType.SCALE // Scalable as requested
        ),

        // Health
        HabitEntity(
            name = "Sleep",
            iconName = "bed", // Or "bedtime", "hotel"
            category = "Health",
            type = HabitType.SCALE // Scalable (duration) as requested
        ),
        HabitEntity(
            name = "Sleep Quality",
            iconName = "star", // Or "sentiment_satisfied", "thumb_up"
            category = "Health",
            type = HabitType.SCALE // Scalable (rating 1-5?) as requested
        ),
        HabitEntity(
            name = "Sport",
            iconName = "fitness_center", // Or "directions_run", "sports_soccer"
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Healthy",
            iconName = "nutrition", // Or "restaurant", "local_florist" (looks like veg)
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Veggie",
            iconName = "eco", // Or "grass"
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Wake Up Early",
            iconName = "wb_sunny", // Or "alarm"
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Drink Alcohol",
            iconName = "local_bar", // Or "wine_bar", "no_drinks" (if tracking avoidance)
            category = "Health",
            type = HabitType.BINARY // Track if *any* alcohol was consumed? Or change to SCALE for quantity?
        ),

        // Social
        HabitEntity(
            name = "Party",
            iconName = "celebration", // Or "liquor"
            category = "Social",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "See Friends",
            iconName = "group",
            category = "Social",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "See Family",
            iconName = "people", // Or "family_restroom", "escalator_warning"
            category = "Social",
            type = HabitType.BINARY
        ),
    )
}

@Database(
    entities = [
        HabitEntryEntity::class,
        HabitEntity::class,
        HabitProgressEntity::class // <<< THIS MUST BE PRESENT!!!
    ],
    version = 4, // Version looks correct
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun habitEntryDao(): HabitEntryDao
    abstract fun habitDao(): HabitDao
    abstract fun habitProgressDao(): HabitProgressDao

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
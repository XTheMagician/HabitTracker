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
            name = "Gaming",
            iconName = "sports_esports",
            category = "Freetime",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Movie",
            iconName = "movie",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Datenight",
            iconName = "favorite",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Trip",
            iconName = "explore",
            category = "Freetime",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Groceries",
            iconName = "shopping_cart",
            category = "Household",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Cook",
            iconName = "soup_kitchen",
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
        HabitEntity(
            name = "Study",
            iconName = "school",
            category = "Work",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Take a Break",
            iconName = "free_breakfast",
            category = "Work",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Sleep",
            iconName = "bed",
            category = "Health",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Sleep Quality",
            iconName = "star",
            category = "Health",
            type = HabitType.SCALE
        ),
        HabitEntity(
            name = "Sport",
            iconName = "fitness_center",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Healthy",
            iconName = "nutrition",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Eat Veggie",
            iconName = "eco",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Wake Up Early",
            iconName = "wb_sunny",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Drink Alcohol",
            iconName = "local_bar",
            category = "Health",
            type = HabitType.BINARY
        ),
        HabitEntity(
            name = "Party",
            iconName = "celebration",
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
            iconName = "people",
            category = "Social",
            type = HabitType.BINARY
        )
    )
}

@Database(
    entities = [
        HabitEntryEntity::class,
        HabitEntity::class,
        HabitProgressEntity::class
    ],
    version = 4,
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
package com.example.habit_tracker.data.db // Adjust package if needed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitEntryDao {

    @Query("SELECT * FROM habit_entries ORDER BY date DESC")
    fun getAllEntries(): Flow<List<HabitEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(entry: HabitEntryEntity)

    @Delete
    suspend fun delete(entry: HabitEntryEntity)

    @Query("DELETE FROM habit_entries WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT * FROM habit_entries WHERE date = :date LIMIT 1")
    suspend fun getEntryByDateOnce(date: String): HabitEntryEntity?

    // Note: This looks like it belongs in HabitProgressDao based on the query
    // If it IS in HabitEntryDao, the table name 'habit_progress' might be incorrect
    // Assuming it's correctly placed for now based on your file content.
    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    suspend fun getProgressForDateOnce(date: String): List<HabitProgressEntity>?

    // Original function for getting mood since a start date
    @Query("SELECT date, mood FROM habit_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getMoodEntriesSince(startDate: String): Flow<List<MoodDataPoint>>

    // Original function for getting all entry details since a start date
    @Query("SELECT * FROM habit_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getAllEntriesSince(startDate: String): Flow<List<HabitEntryEntity>>

    // --- NEW FUNCTION for getting mood data within a specific month ---
    @Query("SELECT date, mood FROM habit_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMoodEntriesForMonth(startDate: String, endDate: String): Flow<List<MoodDataPoint>>
    // --- END NEW FUNCTION ---

}

// Define a simple data class for the query result
data class MoodDataPoint(val date: String, val mood: String)
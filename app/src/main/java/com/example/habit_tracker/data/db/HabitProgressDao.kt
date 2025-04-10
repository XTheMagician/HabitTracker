package com.example.habit_tracker.data.db // Adjust package if needed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitProgressDao {

    // Insert or update progress (handles both binary 'done' and scale levels)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: HabitProgressEntity)

    // Delete progress for a specific habit on a specific date
    @Query("DELETE FROM habit_progress WHERE entryDate = :date AND habitId = :habitId")
    suspend fun deleteProgress(date: String, habitId: Int)

    // Delete ALL progress records for a specific date
    @Query("DELETE FROM habit_progress WHERE entryDate = :date")
    suspend fun deleteAllProgressForDate(date: String)

    // Get progress for a single date (non-Flow)
    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    suspend fun getProgressForDateOnce(date: String): List<HabitProgressEntity>

    // Get progress for a single date (Flow)
    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    fun getHabitProgressForDate(date: String): Flow<List<HabitProgressEntity>>

    // Original function for getting all progress since a start date
    @Query("SELECT * FROM habit_progress WHERE entryDate >= :startDate")
    fun getAllProgressSince(startDate: String): Flow<List<HabitProgressEntity>>

    // --- NEW FUNCTION for getting progress data within a specific month ---
    @Query("SELECT * FROM habit_progress WHERE entryDate BETWEEN :startDate AND :endDate")
    fun getAllProgressForMonth(startDate: String, endDate: String): Flow<List<HabitProgressEntity>>
    // --- END NEW FUNCTION ---

}
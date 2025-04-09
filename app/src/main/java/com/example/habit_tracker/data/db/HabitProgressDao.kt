package com.example.habit_tracker.data.db // Adjust package if needed

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

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

    // In HabitProgressDao.kt
    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    suspend fun getProgressForDateOnce(date: String): List<HabitProgressEntity> // <-- REMOVED Nullable '?'

}
package com.example.habit_tracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: HabitProgressEntity)

    @Query("DELETE FROM habit_progress WHERE entryDate = :date AND habitId = :habitId")
    suspend fun deleteProgress(date: String, habitId: Int)

    @Query("DELETE FROM habit_progress WHERE entryDate = :date")
    suspend fun deleteAllProgressForDate(date: String)

    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    suspend fun getProgressForDateOnce(date: String): List<HabitProgressEntity>

    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    fun getHabitProgressForDate(date: String): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE entryDate >= :startDate")
    fun getAllProgressSince(startDate: String): Flow<List<HabitProgressEntity>>

    @Query("SELECT * FROM habit_progress WHERE entryDate BETWEEN :startDate AND :endDate")
    fun getAllProgressBetweenDates(
        startDate: String,
        endDate: String
    ): Flow<List<HabitProgressEntity>>

}
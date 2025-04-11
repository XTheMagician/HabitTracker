package com.example.habit_tracker.data.db

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

    @Query("SELECT * FROM habit_progress WHERE entryDate = :date")
    suspend fun getProgressForDateOnce(date: String): List<HabitProgressEntity>?

    @Query("SELECT date, mood FROM habit_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getMoodEntriesSince(startDate: String): Flow<List<MoodDataPoint>>

    @Query("SELECT * FROM habit_entries WHERE date >= :startDate ORDER BY date ASC")
    fun getAllEntriesSince(startDate: String): Flow<List<HabitEntryEntity>>

    @Query("SELECT date, mood FROM habit_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMoodEntriesBetweenDates(startDate: String, endDate: String): Flow<List<MoodDataPoint>>

}

data class MoodDataPoint(val date: String, val mood: String)
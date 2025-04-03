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
}

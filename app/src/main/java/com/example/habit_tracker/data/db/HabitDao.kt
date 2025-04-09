package com.example.habit_tracker.data.db // Adjust package if needed

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    // Get all habits as a Flow (for reactive UI)
    @Query("SELECT * FROM habits ORDER BY category, name ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    // Insert a single habit
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    // Insert multiple habits (used for pre-population)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg habits: HabitEntity) // Keep vararg

    // Delete a single habit
    @Delete
    suspend fun delete(habit: HabitEntity)

    // Delete all habits (use with caution)
    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    // Get specific habits by their IDs as a Flow
    @Query("SELECT * FROM habits WHERE id IN (:ids)")
    fun getHabitsByIds(ids: List<Int>): Flow<List<HabitEntity>>

    // Get specific habits by their IDs once (Suspend version)
    @Query("SELECT * FROM habits WHERE id IN (:ids)")
    suspend fun getHabitsByIdsOnce(ids: List<Int>): List<HabitEntity>

}
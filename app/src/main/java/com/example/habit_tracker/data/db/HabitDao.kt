package com.example.habit_tracker.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY category, name ASC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: HabitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg habits: HabitEntity)

    @Delete
    suspend fun delete(habit: HabitEntity)

    @Query("DELETE FROM habits")
    suspend fun deleteAll()

    @Query("SELECT * FROM habits WHERE id IN (:ids)")
    fun getHabitsByIds(ids: List<Int>): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id IN (:ids)")
    suspend fun getHabitsByIdsOnce(ids: List<Int>): List<HabitEntity>

}
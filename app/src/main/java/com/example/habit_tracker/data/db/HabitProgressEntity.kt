package com.example.habit_tracker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "habit_progress",
    // Define composite primary key
    primaryKeys = ["entryDate", "habitId"],
    // Define foreign keys to link to habits and entries
    foreignKeys = [
        ForeignKey(
            entity = HabitEntryEntity::class,
            parentColumns = ["date"], // PK of HabitEntryEntity
            childColumns = ["entryDate"],
            onDelete = ForeignKey.CASCADE // If an entry is deleted, delete associated progress
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"], // PK of HabitEntity
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE // If a habit is deleted, delete associated progress
        )
    ],
    // Add indices for faster lookups
    indices = [Index("entryDate"), Index("habitId")]
)
data class HabitProgressEntity(
    val entryDate: String, // Foreign key to HabitEntryEntity.date
    val habitId: Int,      // Foreign key to HabitEntity.id

    // Store the scalable value as Int?, null for binary habits or not done
    @ColumnInfo(name = "progress_value", defaultValue = "NULL")
    val value: Int? = null
)
package com.example.habit_tracker.data.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "habit_progress",
    primaryKeys = ["entryDate", "habitId"],
    foreignKeys = [
        ForeignKey(
            entity = HabitEntryEntity::class,
            parentColumns = ["date"],
            childColumns = ["entryDate"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("entryDate"), Index("habitId")]
)
data class HabitProgressEntity(
    val entryDate: String,
    val habitId: Int,

    @ColumnInfo(name = "progress_value", defaultValue = "NULL")
    val value: Int? = null
)
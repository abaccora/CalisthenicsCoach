package com.rushd.calisthenicscoach.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val planId: String,
    val title: String,
    val completedAt: Long,
    val durationSec: Int,
    val completedExercises: Int
)

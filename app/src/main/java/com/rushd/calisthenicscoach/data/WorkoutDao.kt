package com.rushd.calisthenicscoach.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workout_history ORDER BY completedAt DESC")
    fun observeAll(): Flow<List<WorkoutHistory>>

    @Insert
    suspend fun insert(item: WorkoutHistory)
}

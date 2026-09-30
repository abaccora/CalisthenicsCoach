package com.rushd.calisthenicscoach.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSessionEntity(
    @PrimaryKey val id: String,
    val planId: String,
    val workoutId: String? = null,
    val readinessCheckId: String? = null,
    val startedAt: Long,
    val endedAt: Long? = null,
    val status: String = "ACTIVE",
    val currentExerciseIndex: Int = 0,
    val perceivedDifficulty: String? = null,
    val painReported: Boolean = false
)

@Entity(
    tableName = "workout_substitutions",
    primaryKeys = ["sessionId", "exerciseIndex"],
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("sessionId")]
)
data class WorkoutSubstitutionEntity(
    val sessionId: String,
    val exerciseIndex: Int,
    val replacementName: String
)

@Entity(
    tableName = "set_logs",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutSessionEntity::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("sessionId"),
        Index("exerciseName"),
        Index(value = ["sessionId", "exerciseIndex", "setIndex"], unique = true)
    ]
)
data class SetLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val exerciseIndex: Int,
    val exerciseName: String,
    val exerciseId: String? = null,
    val setIndex: Int,
    val reps: Int? = null,
    val holdSeconds: Int? = null,
    val leftReps: Int? = null,
    val rightReps: Int? = null,
    val assistance: String? = null,
    val addedWeightKg: Double? = null,
    val bandLevel: String? = null,
    val tempo: String? = null,
    val rpe: Int? = null,
    val rir: Int? = null,
    val completed: Boolean = true,
    val failed: Boolean = false,
    val loggedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "assessment_results")
data class AssessmentResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: String? = null,
    val exerciseId: String? = null,
    val metricId: String,
    val value: Double,
    val unit: String,
    val goalContext: String,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "skill_states")
data class SkillStateEntity(
    @PrimaryKey val skillId: String,
    val athleteId: String = "local",
    val currentNodeId: String,
    val masteryScore: Double = 0.0,
    val successfulSessions: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
)

data class ExerciseHistoryStatRow(
    val exerciseName: String,
    val bestReps: Int,
    val avgRpe: Double?,
    val totalSets: Int
)

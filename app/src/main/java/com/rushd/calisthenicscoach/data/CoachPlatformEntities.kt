package com.rushd.calisthenicscoach.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * Stable Room model for the Coach Platform.
 *
 * DataStore is intentionally kept out of this model; it remains appropriate for
 * small UI preferences such as language, units and theme.
 */
@Entity(tableName = "athlete_profiles")
data class AthleteProfileEntity(
    @androidx.room.PrimaryKey val id: String = "local",
    val age: Int? = null,
    val heightCm: Double? = null,
    val weightKg: Double? = null,
    val dailyActivity: String = "MODERATE",
    val experience: String = "BEGINNER",
    val primaryGoal: String = "GENERAL",
    val trainingDaysPerWeek: Int = 3,
    val sessionMinutes: Int = 45,
    val equipmentCsv: String = "",
    val limitationsCsv: String = "",
    val targetSkillsCsv: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "assessments",
    indices = [Index("athleteId"), Index("recordedAt")]
)
data class AssessmentEntity(
    @androidx.room.PrimaryKey val id: String,
    val athleteId: String = "local",
    val assessmentType: String,
    val goalContext: String,
    val status: String = "COMPLETED",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercises",
    indices = [
        Index(value = ["technicalName"], unique = true),
        Index("movementPattern"),
        Index("sourceActionId")
    ]
)
data class ExerciseEntity(
    @androidx.room.PrimaryKey val id: String,
    val technicalName: String,
    val nameEn: String,
    val nameAr: String,
    val movementPattern: String,
    val difficulty: String,
    val measurementType: String,
    val equipmentCsv: String = "",
    val primaryMusclesCsv: String = "",
    val secondaryMusclesCsv: String = "",
    val instructionsEn: String = "",
    val instructionsAr: String = "",
    val commonMistakesAr: String = "",
    val contraindicationsAr: String = "",
    val videoKey: String? = null,
    val isCoreCatalog: Boolean = false,
    val source: String = "CURATED",
    val sourceActionId: Int? = null,
    val sourceRevision: Int? = null,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "exercise_variants",
    primaryKeys = ["exerciseId", "targetExerciseId", "relation"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetExerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("exerciseId"), Index("targetExerciseId")]
)
data class ExerciseVariantEntity(
    val exerciseId: String,
    val targetExerciseId: String,
    val relation: String,
    val difficultyDelta: Int = 0,
    val equipmentDeltaCsv: String = "",
    val reasonAr: String = ""
)

@Entity(tableName = "skills")
data class SkillEntity(
    @androidx.room.PrimaryKey val id: String,
    val nameEn: String,
    val nameAr: String,
    val descriptionAr: String = "",
    val displayOrder: Int = 0
)

@Entity(
    tableName = "progression_nodes",
    foreignKeys = [
        ForeignKey(
            entity = SkillEntity::class,
            parentColumns = ["id"],
            childColumns = ["skillId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("skillId"), Index("exerciseId")]
)
data class ProgressionNodeEntity(
    @androidx.room.PrimaryKey val id: String,
    val skillId: String,
    val exerciseId: String,
    val orderIndex: Int,
    val entryRuleJson: String = "{}",
    val masteryRuleJson: String = "{}",
    val minimumSessions: Int = 2
)

@Entity(
    tableName = "progression_edges",
    primaryKeys = ["fromNodeId", "toNodeId"],
    foreignKeys = [
        ForeignKey(
            entity = ProgressionNodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromNodeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProgressionNodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["toNodeId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("fromNodeId"), Index("toNodeId")]
)
data class ProgressionEdgeEntity(
    val fromNodeId: String,
    val toNodeId: String,
    val relation: String = "PROGRESSION",
    val priority: Int = 0
)

@Entity(
    tableName = "training_programs",
    indices = [Index("athleteId"), Index("status")]
)
data class TrainingProgramEntity(
    @androidx.room.PrimaryKey val id: String,
    val athleteId: String = "local",
    val title: String,
    val rationaleAr: String,
    val totalWeeks: Int,
    val daysPerWeek: Int,
    val sessionMinutes: Int,
    val status: String = "ACTIVE",
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "program_weeks",
    primaryKeys = ["programId", "weekNumber"],
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId")]
)
data class ProgramWeekEntity(
    val programId: String,
    val weekNumber: Int,
    val phaseId: String,
    val phaseTitleAr: String,
    val phaseGoalAr: String = ""
)

@Entity(
    tableName = "workouts",
    foreignKeys = [
        ForeignKey(
            entity = TrainingProgramEntity::class,
            parentColumns = ["id"],
            childColumns = ["programId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("programId"), Index(value = ["programId", "weekNumber", "dayNumber"])]
)
data class WorkoutEntity(
    @androidx.room.PrimaryKey val id: String,
    val programId: String,
    val weekNumber: Int,
    val dayNumber: Int,
    val titleAr: String,
    val titleEn: String = "",
    val estimatedMinutes: Int,
    val workoutType: String = "STRENGTH"
)

@Entity(
    tableName = "workout_exercises",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workoutId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exerciseId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("workoutId"), Index("exerciseId"), Index(value = ["workoutId", "position"], unique = true)]
)
data class WorkoutExerciseEntity(
    @androidx.room.PrimaryKey val id: String,
    val workoutId: String,
    val exerciseId: String,
    val position: Int,
    val sets: Int,
    val targetRepsMin: Int? = null,
    val targetRepsMax: Int? = null,
    val targetHoldSeconds: Int? = null,
    val targetLeftReps: Int? = null,
    val targetRightReps: Int? = null,
    val targetRpe: Int? = null,
    val targetRir: Int? = null,
    val restSeconds: Int,
    val assistance: String? = null,
    val addedWeightKg: Double? = null,
    val bandLevel: String? = null,
    val tempo: String? = null
)

@Entity(
    tableName = "readiness_checks",
    indices = [Index("athleteId"), Index("recordedAt")]
)
data class ReadinessCheckEntity(
    @androidx.room.PrimaryKey val id: String,
    val athleteId: String = "local",
    val energy: String,
    val timeAvailableMinutes: Int,
    val equipmentAvailableCsv: String = "",
    val soreness: String = "NONE",
    val notes: String = "",
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "performance_history",
    indices = [Index("athleteId"), Index("exerciseId"), Index("recordedAt")]
)
data class PerformanceHistoryEntity(
    @androidx.room.PrimaryKey val id: String,
    val athleteId: String = "local",
    val exerciseId: String,
    val sessionId: String,
    val successfulSets: Int,
    val attemptedSets: Int,
    val bestReps: Int? = null,
    val bestHoldSeconds: Int? = null,
    val totalReps: Int? = null,
    val averageRpe: Double? = null,
    val averageRir: Double? = null,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "personal_bests",
    primaryKeys = ["athleteId", "exerciseId", "metric"],
    indices = [Index("exerciseId")]
)
data class PersonalBestEntity(
    val athleteId: String = "local",
    val exerciseId: String,
    val metric: String,
    val value: Double,
    val unit: String,
    val sessionId: String? = null,
    val achievedAt: Long = System.currentTimeMillis()
)

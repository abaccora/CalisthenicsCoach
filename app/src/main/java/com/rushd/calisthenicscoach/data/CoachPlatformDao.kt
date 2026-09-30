package com.rushd.calisthenicscoach.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachPlatformDao {

    @Upsert
    suspend fun upsertAthleteProfile(profile: AthleteProfileEntity)

    @Query("SELECT * FROM athlete_profiles WHERE id = :athleteId LIMIT 1")
    fun observeAthleteProfile(athleteId: String = "local"): Flow<AthleteProfileEntity?>

    @Upsert
    suspend fun upsertAssessment(assessment: AssessmentEntity)

    @Query("SELECT * FROM assessments WHERE athleteId = :athleteId ORDER BY recordedAt DESC")
    fun observeAssessments(athleteId: String = "local"): Flow<List<AssessmentEntity>>

    @Upsert
    suspend fun upsertExercises(items: List<ExerciseEntity>)

    @Query("SELECT * FROM exercises WHERE isCoreCatalog = 1 ORDER BY movementPattern, difficulty, nameEn")
    fun observeCoreExercises(): Flow<List<ExerciseEntity>>

    @Query("SELECT * FROM exercises WHERE id = :exerciseId LIMIT 1")
    suspend fun getExercise(exerciseId: String): ExerciseEntity?

    @Query("""
        SELECT * FROM exercises
        WHERE source = :source
        ORDER BY sourceActionId
    """)
    suspend fun getExercisesBySource(source: String): List<ExerciseEntity>

    @Upsert
    suspend fun upsertExerciseVariants(items: List<ExerciseVariantEntity>)

    @Query("""
        SELECT * FROM exercise_variants
        WHERE exerciseId = :exerciseId
        ORDER BY difficultyDelta, targetExerciseId
    """)
    suspend fun getExerciseVariants(exerciseId: String): List<ExerciseVariantEntity>

    @Upsert
    suspend fun upsertSkills(items: List<SkillEntity>)

    @Upsert
    suspend fun upsertProgressionNodes(items: List<ProgressionNodeEntity>)

    @Upsert
    suspend fun upsertProgressionEdges(items: List<ProgressionEdgeEntity>)

    @Query("""
        SELECT * FROM progression_nodes
        WHERE skillId = :skillId
        ORDER BY orderIndex
    """)
    fun observeProgressionNodes(skillId: String): Flow<List<ProgressionNodeEntity>>

    @Query("""
        SELECT * FROM progression_edges
        WHERE fromNodeId = :nodeId
        ORDER BY priority DESC
    """)
    suspend fun getOutgoingProgressionEdges(nodeId: String): List<ProgressionEdgeEntity>

    @Upsert
    suspend fun upsertProgram(program: TrainingProgramEntity)

    @Upsert
    suspend fun upsertProgramWeeks(items: List<ProgramWeekEntity>)

    @Upsert
    suspend fun upsertWorkouts(items: List<WorkoutEntity>)

    @Upsert
    suspend fun upsertWorkoutExercises(items: List<WorkoutExerciseEntity>)

    @Query("""
        SELECT * FROM training_programs
        WHERE athleteId = :athleteId AND status = 'ACTIVE'
        ORDER BY generatedAt DESC
        LIMIT 1
    """)
    fun observeActiveProgram(athleteId: String = "local"): Flow<TrainingProgramEntity?>

    @Query("""
        SELECT * FROM workouts
        WHERE programId = :programId
        ORDER BY weekNumber, dayNumber
    """)
    fun observeProgramWorkouts(programId: String): Flow<List<WorkoutEntity>>

    @Query("""
        SELECT * FROM workout_exercises
        WHERE workoutId = :workoutId
        ORDER BY position
    """)
    suspend fun getWorkoutExercises(workoutId: String): List<WorkoutExerciseEntity>

    @Upsert
    suspend fun upsertReadinessCheck(check: ReadinessCheckEntity)

    @Query("""
        SELECT * FROM readiness_checks
        WHERE athleteId = :athleteId
        ORDER BY recordedAt DESC
        LIMIT 1
    """)
    fun observeLatestReadiness(athleteId: String = "local"): Flow<ReadinessCheckEntity?>

    @Upsert
    suspend fun upsertPerformanceHistory(item: PerformanceHistoryEntity)

    @Query("""
        SELECT * FROM performance_history
        WHERE athleteId = :athleteId AND exerciseId = :exerciseId
        ORDER BY recordedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentPerformance(
        athleteId: String = "local",
        exerciseId: String,
        limit: Int = 6
    ): List<PerformanceHistoryEntity>

    @Upsert
    suspend fun upsertPersonalBest(item: PersonalBestEntity)

    @Query("""
        SELECT * FROM personal_bests
        WHERE athleteId = :athleteId
        ORDER BY achievedAt DESC
    """)
    fun observePersonalBests(athleteId: String = "local"): Flow<List<PersonalBestEntity>>
}

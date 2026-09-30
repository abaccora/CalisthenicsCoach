package com.rushd.calisthenicscoach.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CoachDao {
    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    fun observeActiveSession(): Flow<WorkoutSessionEntity?>

    @Query("SELECT * FROM workout_sessions WHERE status = 'ACTIVE' ORDER BY startedAt DESC LIMIT 1")
    suspend fun getActiveSession(): WorkoutSessionEntity?

    @Upsert
    suspend fun upsertSession(session: WorkoutSessionEntity)

    @Query("UPDATE workout_sessions SET status = 'COMPLETED', endedAt = :endedAt WHERE id = :sessionId")
    suspend fun completeSession(sessionId: String, endedAt: Long)

    @Query("UPDATE workout_sessions SET currentExerciseIndex = :index WHERE id = :sessionId")
    suspend fun updateCurrentExercise(sessionId: String, index: Int)

    @Query("SELECT * FROM workout_sessions WHERE id = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: String): WorkoutSessionEntity?

    @Query("""
        UPDATE workout_sessions
        SET perceivedDifficulty = :difficulty, painReported = :painReported
        WHERE id = :sessionId
    """)
    suspend fun updateSessionFeedback(
        sessionId: String,
        difficulty: String?,
        painReported: Boolean
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetLogs(logs: List<SetLogEntity>)

    @Query("DELETE FROM set_logs WHERE sessionId = :sessionId")
    suspend fun deleteSetLogs(sessionId: String)

    @Query("SELECT * FROM set_logs WHERE sessionId = :sessionId ORDER BY exerciseIndex, setIndex")
    suspend fun getSetLogs(sessionId: String): List<SetLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubstitutions(items: List<WorkoutSubstitutionEntity>)

    @Query("DELETE FROM workout_substitutions WHERE sessionId = :sessionId")
    suspend fun deleteSubstitutions(sessionId: String)

    @Query("SELECT * FROM workout_substitutions WHERE sessionId = :sessionId ORDER BY exerciseIndex")
    suspend fun getSubstitutions(sessionId: String): List<WorkoutSubstitutionEntity>

    @Transaction
    suspend fun replaceSessionDetails(
        sessionId: String,
        logs: List<SetLogEntity>,
        substitutions: List<WorkoutSubstitutionEntity>
    ) {
        deleteSetLogs(sessionId)
        if (logs.isNotEmpty()) insertSetLogs(logs)
        deleteSubstitutions(sessionId)
        if (substitutions.isNotEmpty()) insertSubstitutions(substitutions)
    }

    @Query("""
        SELECT s.exerciseName AS exerciseName,
               MAX(COALESCE(s.reps, 0)) AS bestReps,
               AVG(s.rpe) AS avgRpe,
               COUNT(*) AS totalSets
        FROM set_logs s
        INNER JOIN workout_sessions w ON w.id = s.sessionId
        WHERE w.status = 'COMPLETED' AND s.completed = 1
        GROUP BY s.exerciseName
        ORDER BY MAX(s.loggedAt) DESC
    """)
    fun observeExerciseHistory(): Flow<List<ExerciseHistoryStatRow>>

    @Insert
    suspend fun insertAssessmentResults(items: List<AssessmentResultEntity>)

    @Query("SELECT * FROM assessment_results ORDER BY recordedAt DESC")
    fun observeAssessments(): Flow<List<AssessmentResultEntity>>

    @Upsert
    suspend fun upsertSkillState(state: SkillStateEntity)

    @Query("SELECT * FROM skill_states")
    fun observeSkillStates(): Flow<List<SkillStateEntity>>

    @Query("""
        SELECT s.* FROM set_logs s
        INNER JOIN workout_sessions w ON w.id = s.sessionId
        WHERE w.status = 'COMPLETED' AND s.exerciseName = :exerciseName
        ORDER BY s.loggedAt DESC
        LIMIT :limit
    """)
    suspend fun getRecentSetLogsForExercise(
        exerciseName: String,
        limit: Int = 60
    ): List<SetLogEntity>
}

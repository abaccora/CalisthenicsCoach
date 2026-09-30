package com.rushd.calisthenicscoach.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID

data class CoachActiveSession(
    val id: String,
    val planId: String,
    val currentExerciseIndex: Int,
    val startedAt: Long,
    val setLogs: List<SetLogEntity>,
    val substitutions: Map<Int, String>
)

class CoachRepository(context: Context) {

    private val dao = AppDatabase.get(context).coachDao()

    fun observeHistory(): Flow<List<ExerciseHistoryStat>> =
        dao.observeExerciseHistory().map { rows ->
            rows.map { row ->
                ExerciseHistoryStat(
                    exerciseName = row.exerciseName,
                    bestReps = row.bestReps,
                    avgRpe = row.avgRpe ?: 0.0,
                    totalSets = row.totalSets,
                    updatedAt = 0L
                )
            }
        }

    fun observeSkillStates(): Flow<List<SkillStateEntity>> = dao.observeSkillStates()

    fun observeAssessments(): Flow<List<AssessmentResultEntity>> = dao.observeAssessments()

    suspend fun loadActiveSession(): CoachActiveSession? {
        val session = dao.getActiveSession() ?: return null
        return CoachActiveSession(
            id = session.id,
            planId = session.planId,
            currentExerciseIndex = session.currentExerciseIndex,
            startedAt = session.startedAt,
            setLogs = dao.getSetLogs(session.id),
            substitutions = dao.getSubstitutions(session.id)
                .associate { it.exerciseIndex to it.replacementName }
        )
    }

    suspend fun startSession(planId: String): CoachActiveSession {
        val existing = loadActiveSession()
        if (existing != null && existing.planId == planId) return existing

        val id = UUID.randomUUID().toString()
        val startedAt = System.currentTimeMillis()
        dao.upsertSession(
            WorkoutSessionEntity(
                id = id,
                planId = planId,
                startedAt = startedAt
            )
        )
        return CoachActiveSession(
            id = id,
            planId = planId,
            currentExerciseIndex = 0,
            startedAt = startedAt,
            setLogs = emptyList(),
            substitutions = emptyMap()
        )
    }

    suspend fun saveSessionProgress(
        sessionId: String,
        currentExerciseIndex: Int,
        setLogs: List<SetLogEntity>,
        substitutions: Map<Int, String>
    ) {
        dao.updateCurrentExercise(sessionId, currentExerciseIndex)
        dao.replaceSessionDetails(
            sessionId = sessionId,
            logs = setLogs,
            substitutions = substitutions.map { (index, name) ->
                WorkoutSubstitutionEntity(
                    sessionId = sessionId,
                    exerciseIndex = index,
                    replacementName = name
                )
            }
        )
    }

    suspend fun completeSession(sessionId: String) {
        dao.completeSession(sessionId, System.currentTimeMillis())
    }

    suspend fun recordAssessment(
        goalContext: String,
        values: Map<String, Pair<Double, String>>
    ) {
        dao.insertAssessmentResults(
            values.map { (metricId, valueAndUnit) ->
                AssessmentResultEntity(
                    metricId = metricId,
                    value = valueAndUnit.first,
                    unit = valueAndUnit.second,
                    goalContext = goalContext
                )
            }
        )
    }

    suspend fun updateSkillState(
        skillId: String,
        nodeId: String,
        masteryScore: Double,
        successfulSessions: Int
    ) {
        dao.upsertSkillState(
            SkillStateEntity(
                skillId = skillId,
                currentNodeId = nodeId,
                masteryScore = masteryScore.coerceIn(0.0, 1.0),
                successfulSessions = successfulSessions,
                updatedAt = System.currentTimeMillis()
            )
        )
    }
}

package com.rushd.calisthenicscoach.data

import com.rushd.calisthenicscoach.domain.CoachDecision
import com.rushd.calisthenicscoach.domain.MeasurementType
import com.rushd.calisthenicscoach.domain.PerformanceSample
import com.rushd.calisthenicscoach.domain.ProgressionEngine
import com.rushd.calisthenicscoach.domain.SessionPerformance
import com.rushd.calisthenicscoach.domain.SkillProgressionGraph

data class SkillAdaptationResult(
    val skillId: String,
    val fromNodeId: String,
    val toNodeId: String,
    val decision: CoachDecision,
    val reasonAr: String
)

class CoachAdaptationRepository(private val db: AppDatabase) {
    private val dao = db.coachDao()

    suspend fun updateSkillStatesAfterWorkout(): List<SkillAdaptationResult> {
        val states = dao.observeSkillStates().firstValue()
        val results = mutableListOf<SkillAdaptationResult>()

        states.forEach { state ->
            val node = SkillProgressionGraph.node(state.currentNodeId) ?: return@forEach
            val exerciseName = node.exerciseName ?: return@forEach
            val logs = dao.getRecentSetLogsForExercise(exerciseName)
            if (logs.isEmpty()) return@forEach

            val sessions = logs
                .groupBy { it.sessionId }
                .map { (_, setLogs) ->
                    SessionPerformance(
                        nodeId = node.id,
                        samples = setLogs
                            .sortedBy { it.setIndex }
                            .map { log ->
                                PerformanceSample(
                                    measurement = node.mastery.measurement,
                                    value = when (node.mastery.measurement) {
                                        MeasurementType.HOLD_SECONDS -> log.holdSeconds ?: log.reps ?: 0
                                        MeasurementType.LEFT_RIGHT_REPS,
                                        MeasurementType.UNILATERAL_REPS -> minOf(
                                            log.leftReps ?: log.reps ?: 0,
                                            log.rightReps ?: log.reps ?: 0
                                        )
                                        else -> log.reps ?: 0
                                    },
                                    rpe = log.rpe,
                                    completed = log.completed
                                )
                            },
                        completedAt = setLogs.maxOfOrNull { it.loggedAt } ?: 0L
                    )
                }

            val recommendation = ProgressionEngine.evaluate(node.id, sessions)
            val targetId = recommendation.targetNode?.id ?: node.id
            val successful = sessions.count { session ->
                val samples = session.samples.take(node.mastery.sets)
                samples.size >= node.mastery.sets &&
                    samples.all { sample ->
                        sample.value >= node.mastery.target &&
                            (sample.rpe == null || sample.rpe <= node.mastery.maxRpe)
                    }
            }

            dao.upsertSkillState(
                state.copy(
                    currentNodeId = targetId,
                    masteryScore = masteryScore(node.mastery.target, sessions),
                    successfulSessions = successful,
                    updatedAt = System.currentTimeMillis()
                )
            )

            results += SkillAdaptationResult(
                skillId = state.skillId,
                fromNodeId = node.id,
                toNodeId = targetId,
                decision = recommendation.decision,
                reasonAr = recommendation.reasonAr
            )
        }

        return results
    }

    private fun masteryScore(
        target: Int,
        sessions: List<SessionPerformance>
    ): Double {
        val recent = sessions.take(3).flatMap { it.samples }
        if (recent.isEmpty() || target <= 0) return 0.0
        return recent
            .map { (it.value.toDouble() / target.toDouble()).coerceIn(0.0, 1.0) }
            .average()
    }
}

private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstValue(): T =
    kotlinx.coroutines.flow.first(this)

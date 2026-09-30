package com.rushd.calisthenicscoach.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class SetPerformance(
    val exerciseName: String,
    val setIndex: Int,
    val reps: Int,
    val rpe: Int,
    val holdSeconds: Int? = null,
    val leftReps: Int? = null,
    val rightReps: Int? = null,
    val assistance: String? = null,
    val addedWeightKg: Double? = null,
    val bandLevel: String? = null,
    val tempo: String? = null,
    val rir: Int? = null
)

data class ActiveWorkoutSession(
    val planId: String,
    val currentExerciseIndex: Int,
    val completedSets: Map<Int, Int>,
    val setPerformances: List<SetPerformance>,
    val startedAt: Long,
    val substitutions: Map<Int, String> = emptyMap()
)

data class ExerciseHistoryStat(
    val exerciseName: String,
    val bestReps: Int,
    val avgRpe: Double,
    val totalSets: Int,
    val updatedAt: Long
)

class WorkoutSessionRepository(context: Context) {

    private val dao = AppDatabase.get(context).coachDao()

    val activeSession: Flow<ActiveWorkoutSession?> = dao.observeActiveSession().map { session ->
        session?.let { entity ->
            val logs = dao.getSetLogs(entity.id)
            val substitutions = dao.getSubstitutions(entity.id)
            ActiveWorkoutSession(
                planId = entity.planId,
                currentExerciseIndex = entity.currentExerciseIndex,
                completedSets = logs
                    .filter { it.completed }
                    .groupingBy { it.exerciseIndex }
                    .eachCount(),
                setPerformances = logs
                    .filter { it.completed }
                    .map {
                        SetPerformance(
                            exerciseName = it.exerciseName,
                            setIndex = it.setIndex,
                            reps = it.reps ?: 0,
                            rpe = it.rpe ?: 7,
                            holdSeconds = it.holdSeconds,
                            leftReps = it.leftReps,
                            rightReps = it.rightReps,
                            assistance = it.assistance,
                            addedWeightKg = it.addedWeightKg,
                            bandLevel = it.bandLevel,
                            tempo = it.tempo,
                            rir = it.rir
                        )
                    },
                startedAt = entity.startedAt,
                substitutions = substitutions.associate {
                    it.exerciseIndex to it.replacementName
                }
            )
        }
    }

    val history: Flow<List<ExerciseHistoryStat>> = dao.observeExerciseHistory().map { rows ->
        rows.map {
            ExerciseHistoryStat(
                exerciseName = it.exerciseName,
                bestReps = it.bestReps,
                avgRpe = it.avgRpe ?: 0.0,
                totalSets = it.totalSets,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    suspend fun saveSession(session: ActiveWorkoutSession) {
        val id = sessionId(session)
        dao.upsertSession(
            WorkoutSessionEntity(
                id = id,
                planId = session.planId,
                startedAt = session.startedAt,
                status = "ACTIVE",
                currentExerciseIndex = session.currentExerciseIndex
            )
        )

        val logs = session.setPerformances.map { item ->
            SetLogEntity(
                sessionId = id,
                exerciseIndex = inferExerciseIndex(session, item.exerciseName),
                exerciseName = item.exerciseName,
                setIndex = item.setIndex,
                reps = item.reps,
                holdSeconds = item.holdSeconds,
                leftReps = item.leftReps,
                rightReps = item.rightReps,
                assistance = item.assistance,
                addedWeightKg = item.addedWeightKg,
                bandLevel = item.bandLevel,
                tempo = item.tempo,
                rpe = item.rpe,
                rir = item.rir,
                completed = true
            )
        }

        val replacements = session.substitutions.map { (exerciseIndex, name) ->
            WorkoutSubstitutionEntity(
                sessionId = id,
                exerciseIndex = exerciseIndex,
                replacementName = name
            )
        }

        dao.replaceSessionDetails(id, logs, replacements)
    }

    suspend fun clearSession() {
        val active = dao.getActiveSession() ?: return
        dao.completeSession(active.id, System.currentTimeMillis())
    }

    suspend fun mergeHistory(performances: List<SetPerformance>) {
        // History is derived directly from completed Room set logs.
    }

    private fun sessionId(session: ActiveWorkoutSession): String =
        "${session.planId}_${session.startedAt}"

    private fun inferExerciseIndex(
        session: ActiveWorkoutSession,
        exerciseName: String
    ): Int {
        val replacementIndex = session.substitutions.entries
            .firstOrNull { it.value == exerciseName }
            ?.key
        if (replacementIndex != null) return replacementIndex

        val completedCandidates = session.completedSets.keys.sorted()
        return completedCandidates.firstOrNull { index ->
            session.setPerformances.any {
                it.exerciseName == exerciseName && it.setIndex < (session.completedSets[index] ?: 0)
            }
        } ?: session.currentExerciseIndex
    }
}

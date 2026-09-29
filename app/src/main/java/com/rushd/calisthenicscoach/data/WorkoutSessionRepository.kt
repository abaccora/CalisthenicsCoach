package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.workoutSessionStore by preferencesDataStore(name = "workout_session_v1")

data class SetPerformance(
    val exerciseName: String,
    val setIndex: Int,
    val reps: Int,
    val rpe: Int
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

class WorkoutSessionRepository(private val context: Context) {

    private object Keys {
        val active = stringPreferencesKey("active_session")
        val history = stringPreferencesKey("exercise_history")
    }

    val activeSession: Flow<ActiveWorkoutSession?> = context.workoutSessionStore.data.map { prefs ->
        prefs[Keys.active]?.let(::decodeSession)
    }

    val history: Flow<List<ExerciseHistoryStat>> = context.workoutSessionStore.data.map { prefs ->
        prefs[Keys.history]?.let(::decodeHistory) ?: emptyList()
    }

    suspend fun saveSession(session: ActiveWorkoutSession) {
        context.workoutSessionStore.edit { prefs ->
            prefs[Keys.active] = encodeSession(session)
        }
    }

    suspend fun clearSession() {
        context.workoutSessionStore.edit { prefs ->
            prefs.remove(Keys.active)
        }
    }

    suspend fun mergeHistory(performances: List<SetPerformance>) {
        context.workoutSessionStore.edit { prefs ->
            val current = prefs[Keys.history]?.let(::decodeHistory).orEmpty().associateBy { it.exerciseName }.toMutableMap()
            performances.groupBy { it.exerciseName }.forEach { (name, sets) ->
                val previous = current[name]
                val totalSets = (previous?.totalSets ?: 0) + sets.size
                val previousWeighted = (previous?.avgRpe ?: 0.0) * (previous?.totalSets ?: 0)
                val newAverage = if (totalSets > 0) {
                    (previousWeighted + sets.sumOf { it.rpe }.toDouble()) / totalSets
                } else 0.0
                current[name] = ExerciseHistoryStat(
                    exerciseName = name,
                    bestReps = maxOf(previous?.bestReps ?: 0, sets.maxOfOrNull { it.reps } ?: 0),
                    avgRpe = newAverage,
                    totalSets = totalSets,
                    updatedAt = System.currentTimeMillis()
                )
            }
            prefs[Keys.history] = encodeHistory(current.values.toList())
        }
    }

    private fun encodeSession(session: ActiveWorkoutSession): String {
        val completed = JSONObject()
        session.completedSets.forEach { (k, v) -> completed.put(k.toString(), v) }

        val substitutions = JSONObject()
        session.substitutions.forEach { (k, v) -> substitutions.put(k.toString(), v) }

        val performances = JSONArray()
        session.setPerformances.forEach { item ->
            performances.put(JSONObject().apply {
                put("exerciseName", item.exerciseName)
                put("setIndex", item.setIndex)
                put("reps", item.reps)
                put("rpe", item.rpe)
            })
        }

        return JSONObject().apply {
            put("planId", session.planId)
            put("currentExerciseIndex", session.currentExerciseIndex)
            put("completedSets", completed)
            put("setPerformances", performances)
            put("startedAt", session.startedAt)
            put("substitutions", substitutions)
        }.toString()
    }

    private fun decodeSession(raw: String): ActiveWorkoutSession? = runCatching {
        val obj = JSONObject(raw)
        val completedObj = obj.optJSONObject("completedSets") ?: JSONObject()
        val completed = mutableMapOf<Int, Int>()
        completedObj.keys().forEach { key -> completed[key.toInt()] = completedObj.getInt(key) }

        val substitutionsObj = obj.optJSONObject("substitutions") ?: JSONObject()
        val substitutions = mutableMapOf<Int, String>()
        substitutionsObj.keys().forEach { key -> substitutions[key.toInt()] = substitutionsObj.getString(key) }

        val performancesArray = obj.optJSONArray("setPerformances") ?: JSONArray()
        val performances = buildList {
            for (i in 0 until performancesArray.length()) {
                val item = performancesArray.getJSONObject(i)
                add(
                    SetPerformance(
                        exerciseName = item.getString("exerciseName"),
                        setIndex = item.getInt("setIndex"),
                        reps = item.getInt("reps"),
                        rpe = item.getInt("rpe")
                    )
                )
            }
        }

        ActiveWorkoutSession(
            planId = obj.getString("planId"),
            currentExerciseIndex = obj.optInt("currentExerciseIndex", 0),
            completedSets = completed,
            setPerformances = performances,
            startedAt = obj.optLong("startedAt", System.currentTimeMillis()),
            substitutions = substitutions
        )
    }.getOrNull()

    private fun encodeHistory(items: List<ExerciseHistoryStat>): String {
        val array = JSONArray()
        items.forEach { item ->
            array.put(JSONObject().apply {
                put("exerciseName", item.exerciseName)
                put("bestReps", item.bestReps)
                put("avgRpe", item.avgRpe)
                put("totalSets", item.totalSets)
                put("updatedAt", item.updatedAt)
            })
        }
        return array.toString()
    }

    private fun decodeHistory(raw: String): List<ExerciseHistoryStat> = runCatching {
        val array = JSONArray(raw)
        buildList {
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                add(
                    ExerciseHistoryStat(
                        exerciseName = item.getString("exerciseName"),
                        bestReps = item.optInt("bestReps", 0),
                        avgRpe = item.optDouble("avgRpe", 0.0),
                        totalSets = item.optInt("totalSets", 0),
                        updatedAt = item.optLong("updatedAt", 0L)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}

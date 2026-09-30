package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rushd.calisthenicscoach.domain.CoachDecision
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoachAdaptationIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CoachDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.coachDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun repeatedMasteryProgressesSkillStateThroughRoomHistory() = runBlocking {
        dao.upsertSkillState(
            SkillStateEntity(
                skillId = "pull_up",
                currentNodeId = "pull_regular"
            )
        )

        insertCompletedPullSession(
            id = "session_1",
            startedAt = 100L,
            reps = 8,
            rpe = 7,
            difficulty = "مناسبة"
        )
        insertCompletedPullSession(
            id = "session_2",
            startedAt = 200L,
            reps = 8,
            rpe = 7,
            difficulty = "سهلة"
        )

        val result = CoachAdaptationRepository(database)
            .updateSkillStatesAfterWorkout()
            .single { it.skillId == "pull_up" }

        val state = dao.observeSkillStates().first().single { it.skillId == "pull_up" }

        assertEquals(CoachDecision.PROGRESS_VARIATION, result.decision)
        assertEquals("pull_regular", result.fromNodeId)
        assertEquals("pull_explosive", result.toNodeId)
        assertEquals("pull_explosive", state.currentNodeId)
        assertEquals(2, state.successfulSessions)
    }

    @Test
    fun painFeedbackRegressesSkillState() = runBlocking {
        dao.upsertSkillState(
            SkillStateEntity(
                skillId = "pull_up",
                currentNodeId = "pull_regular"
            )
        )

        insertCompletedPullSession(
            id = "pain_session",
            startedAt = 300L,
            reps = 8,
            rpe = 7,
            difficulty = "مناسبة",
            painReported = true
        )

        val result = CoachAdaptationRepository(database)
            .updateSkillStatesAfterWorkout()
            .single { it.skillId == "pull_up" }

        val state = dao.observeSkillStates().first().single { it.skillId == "pull_up" }

        assertEquals(CoachDecision.REGRESS_VARIATION, result.decision)
        assertEquals("pull_assisted", result.toNodeId)
        assertEquals("pull_assisted", state.currentNodeId)
    }

    private suspend fun insertCompletedPullSession(
        id: String,
        startedAt: Long,
        reps: Int,
        rpe: Int,
        difficulty: String,
        painReported: Boolean = false
    ) {
        dao.upsertSession(
            WorkoutSessionEntity(
                id = id,
                planId = "pull_plan",
                startedAt = startedAt,
                endedAt = startedAt + 60_000L,
                status = "COMPLETED",
                currentExerciseIndex = 0,
                perceivedDifficulty = difficulty,
                painReported = painReported
            )
        )

        dao.insertSetLogs(
            List(3) { setIndex ->
                SetLogEntity(
                    sessionId = id,
                    exerciseIndex = 0,
                    exerciseName = "Regular Pull Up",
                    setIndex = setIndex,
                    reps = reps,
                    rpe = rpe,
                    completed = true,
                    loggedAt = startedAt + setIndex
                )
            }
        )
    }
}

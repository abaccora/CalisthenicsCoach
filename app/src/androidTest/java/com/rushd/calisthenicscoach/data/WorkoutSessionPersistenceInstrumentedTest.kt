package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WorkoutSessionPersistenceInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: WorkoutSessionRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkoutSessionRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun activeSessionRestoresProgressSetsAndSubstitutions() = runBlocking {
        val session = ActiveWorkoutSession(
            planId = "strength_b",
            currentExerciseIndex = 2,
            completedSets = mapOf(0 to 2, 1 to 1),
            setPerformances = listOf(
                SetPerformance(
                    exerciseIndex = 0,
                    exerciseName = "Regular Pull Up",
                    setIndex = 0,
                    reps = 6,
                    rpe = 7
                ),
                SetPerformance(
                    exerciseIndex = 0,
                    exerciseName = "Regular Pull Up",
                    setIndex = 1,
                    reps = 5,
                    rpe = 8
                ),
                SetPerformance(
                    exerciseIndex = 1,
                    exerciseName = "Chest Dip",
                    setIndex = 0,
                    reps = 8,
                    rpe = 7
                )
            ),
            startedAt = 123456L,
            substitutions = mapOf(2 to "Bulgarian Split Squat")
        )

        repository.saveSession(session)
        val restored = repository.activeSession.first()

        assertNotNull(restored)
        assertEquals(session.planId, restored?.planId)
        assertEquals(2, restored?.currentExerciseIndex)
        assertEquals(2, restored?.completedSets?.get(0))
        assertEquals(1, restored?.completedSets?.get(1))
        assertEquals(3, restored?.setPerformances?.size)
        assertEquals("Bulgarian Split Squat", restored?.substitutions?.get(2))
    }

    @Test
    fun feedbackIsStoredBeforeSessionIsCompleted() = runBlocking {
        val session = ActiveWorkoutSession(
            planId = "foundation_a",
            currentExerciseIndex = 0,
            completedSets = mapOf(0 to 1),
            setPerformances = listOf(
                SetPerformance(
                    exerciseIndex = 0,
                    exerciseName = "Regular Push Up",
                    setIndex = 0,
                    reps = 10,
                    rpe = 9
                )
            ),
            startedAt = 777L
        )

        repository.saveSession(session)
        repository.saveFeedback("صعبة جدًا", painReported = true)
        repository.clearSession()

        assertNull(repository.activeSession.first())

        val stored = database.coachDao().getSession("foundation_a_777")
        assertNotNull(stored)
        assertEquals("COMPLETED", stored?.status)
        assertEquals("صعبة جدًا", stored?.perceivedDifficulty)
        assertEquals(true, stored?.painReported)
        assertNotNull(stored?.endedAt)
    }
}

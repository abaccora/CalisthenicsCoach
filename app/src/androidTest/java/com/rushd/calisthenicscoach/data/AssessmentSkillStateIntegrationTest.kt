package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rushd.calisthenicscoach.domain.SkillStateEngine
import com.rushd.calisthenicscoach.domain.UserProfile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AssessmentSkillStateIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: CoachRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = CoachRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun measuredAssessmentPersistsAndSeedsSkillGraphState() = runBlocking {
        val profile = UserProfile(
            goal = "Pull-up",
            equipment = setOf("Pull-up Bar", "Parallel Bars")
        )
        val results = listOf(
            AssessmentResultEntity(
                metricId = "push_reps",
                value = 20.0,
                unit = "reps",
                goalContext = profile.goal
            ),
            AssessmentResultEntity(
                metricId = "hang_seconds",
                value = 35.0,
                unit = "seconds",
                goalContext = profile.goal
            ),
            AssessmentResultEntity(
                metricId = "pull_reps",
                value = 8.0,
                unit = "reps",
                goalContext = profile.goal
            ),
            AssessmentResultEntity(
                metricId = "dip_reps",
                value = 8.0,
                unit = "reps",
                goalContext = profile.goal
            )
        )

        repository.saveAssessment(results)
        repository.saveSkillStates(
            SkillStateEngine.initialStates(profile, results)
        )

        val storedAssessment = repository.assessments.first()
        val storedStates = repository.skillStates.first()

        assertEquals(4, storedAssessment.size)
        assertEquals(
            8.0,
            storedAssessment.first { it.metricId == "pull_reps" }.value,
            0.0
        )
        assertEquals(
            "pull_explosive",
            storedStates.first { it.skillId == "pull_up" }.currentNodeId
        )
        assertEquals(
            "dip_regular",
            storedStates.first { it.skillId == "dip" }.currentNodeId
        )
    }
}

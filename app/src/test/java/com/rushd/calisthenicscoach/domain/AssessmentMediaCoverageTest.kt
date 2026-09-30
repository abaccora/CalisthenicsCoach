package com.rushd.calisthenicscoach.domain

import org.junit.Assert.assertNotNull
import org.junit.Test

class AssessmentMediaCoverageTest {

    @Test
    fun allAssessmentMetricsHaveDemonstrationMedia() {
        val profiles = listOf(
            UserProfile(),
            UserProfile(
                goal = "Pull-up",
                equipment = setOf("Pull-up Bar")
            ),
            UserProfile(
                goal = "Muscle-up",
                equipment = setOf("Pull-up Bar", "Parallel Bars")
            ),
            UserProfile(
                goal = "Handstand"
            ),
            UserProfile(
                goal = "Planche"
            )
        )

        profiles.forEach { profile ->
            AssessmentEngine.build(profile).metrics.forEach { metric ->
                val exerciseName = metric.exerciseName
                if (exerciseName != null) {
                    assertNotNull(
                        "Missing assessment media for ${metric.id}: $exerciseName",
                        ExerciseVideoCatalog.forExercise(exerciseName)
                    )
                }
            }
        }
    }
}

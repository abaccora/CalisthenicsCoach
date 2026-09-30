package com.rushd.calisthenicscoach.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoCoverageTest {

    private val profiles = listOf(
        UserProfile(goal = "قوة عامة", daysPerWeek = 3),
        UserProfile(
            goal = "Pull-up",
            equipment = setOf("Pull-up Bar"),
            maxPushUps = 10,
            maxPullUps = 0
        ),
        UserProfile(
            goal = "Muscle-up",
            equipment = setOf("Pull-up Bar", "Parallel Bars"),
            maxPushUps = 20,
            maxPullUps = 5,
            maxDips = 6
        ),
        UserProfile(goal = "Handstand", maxPushUps = 15),
        UserProfile(goal = "Planche", maxPushUps = 20),
        UserProfile(goal = "Front Lever", equipment = setOf("Pull-up Bar"), maxPullUps = 4)
    )

    @Test
    fun everyAssessmentStepHasVerifiedVideo() {
        profiles.forEach { profile ->
            val plan = AssessmentEngine.build(profile)
            assertTrue("Assessment must not be empty for ${profile.goal}", plan.metrics.isNotEmpty())
            plan.metrics.forEach { metric ->
                val exerciseName = metric.exerciseName
                assertTrue("Assessment metric ${metric.id} must declare an exercise", !exerciseName.isNullOrBlank())
                assertNotNull(
                    "Missing video for assessment metric ${metric.id}: $exerciseName",
                    ExerciseVideoCatalog.forExercise(exerciseName!!)
                )
            }
        }
    }

    @Test
    fun everyGeneratedWorkoutExerciseHasVerifiedVideo() {
        profiles.forEach { profile ->
            val blueprint = ProgramEngine.build(profile)
            assertTrue("Program must contain plans for ${profile.goal}", blueprint.plans.isNotEmpty())
            blueprint.plans.forEach { plan ->
                assertTrue("Workout plan ${plan.id} must not be empty", plan.exercises.isNotEmpty())
                plan.exercises.forEach { exercise ->
                    assertNotNull(
                        "Missing video for generated exercise ${exercise.name}",
                        ExerciseVideoCatalog.forExercise(exercise.name)
                    )
                }
            }
        }
    }
}

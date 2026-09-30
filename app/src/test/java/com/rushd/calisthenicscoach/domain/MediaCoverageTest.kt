package com.rushd.calisthenicscoach.domain

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaCoverageTest {

    @Test
    fun everyAssessmentMetricHasPlayableVideo() {
        val profiles = listOf(
            UserProfile(),
            UserProfile(equipment = setOf("Pull-up Bar"), goal = "Pull-up"),
            UserProfile(equipment = setOf("Parallel Bars"), goal = "Dip"),
            UserProfile(equipment = setOf("Pull-up Bar", "Parallel Bars"), goal = "Muscle-up"),
            UserProfile(goal = "Handstand"),
            UserProfile(goal = "Planche")
        )

        profiles.forEach { profile ->
            val plan = AssessmentEngine.build(profile)
            assertTrue("Assessment must not be empty for ${profile.goal}", plan.metrics.isNotEmpty())
            plan.metrics.forEach { metric ->
                val name = metric.exerciseName
                assertNotNull("Assessment metric ${metric.id} has no exercise", name)
                assertNotNull(
                    "Assessment exercise $name has no playable video",
                    ExerciseVideoCatalog.forExercise(name!!)
                )
            }
        }
    }

    @Test
    fun everyGeneratedWorkoutExerciseHasPlayableVideo() {
        val profiles = listOf(
            UserProfile(daysPerWeek = 3),
            UserProfile(daysPerWeek = 3, equipment = setOf("Pull-up Bar"), goal = "Pull-up", maxPullUps = 5),
            UserProfile(daysPerWeek = 3, equipment = setOf("Parallel Bars"), maxDips = 6),
            UserProfile(daysPerWeek = 3, goal = "Handstand", maxPushUps = 15)
        )

        profiles.forEach { profile ->
            val blueprint = ProgramEngine.build(profile)
            assertTrue("No workout plans generated for ${profile.goal}", blueprint.plans.isNotEmpty())
            blueprint.plans.forEach { plan ->
                assertTrue("Workout ${plan.id} is empty", plan.exercises.isNotEmpty())
                plan.exercises.forEach { exercise ->
                    assertNotNull(
                        "Workout exercise ${exercise.name} has no playable video",
                        ExerciseVideoCatalog.forExercise(exercise.name)
                    )
                }
            }
        }
    }

    @Test
    fun modernPackTakesPriorityForVerifiedMoves() {
        val deadBug = ExerciseVideoCatalog.forExercise("Dead Bug")
        val birdDog = ExerciseVideoCatalog.forExercise("Alternating Superman")
        val squat = ExerciseVideoCatalog.forExercise("Air Squat")

        assertTrue(deadBug?.source?.startsWith("HOME_WORKOUT") == true)
        assertTrue(birdDog?.source?.startsWith("HOME_WORKOUT") == true)
        assertTrue(squat?.source?.startsWith("HOME_WORKOUT") == true)
    }
}

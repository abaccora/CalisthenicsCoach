package com.rushd.calisthenicscoach.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ProgressionEngineTest {

    @Test
    fun progressesWhenMasteryIsRepeated() {
        val sessions = listOf(
            mastered("pull_3", reps = 8, rpe = 7, time = 3),
            mastered("pull_3", reps = 8, rpe = 8, time = 2),
            mastered("pull_3", reps = 7, rpe = 8, time = 1)
        )

        val result = ProgressionEngine.evaluate("pull_3", sessions)

        assertEquals(CoachDecision.PROGRESS_VARIATION, result.decision)
        assertEquals("pull_4", result.targetNode?.id)
    }

    @Test
    fun deloadsWhenEffortIsHighAndCompletionLow() {
        val sessions = listOf(
            partial("pull_3", reps = 4, rpe = 10, time = 3),
            partial("pull_3", reps = 4, rpe = 9, time = 2),
            partial("pull_3", reps = 5, rpe = 10, time = 1)
        )

        val result = ProgressionEngine.evaluate("pull_3", sessions)

        assertEquals(CoachDecision.DELOAD, result.decision)
        assertEquals(30, result.restAdjustmentSec)
    }

    @Test
    fun regressesWhenCompletionIsVeryLow() {
        val sessions = listOf(
            partial("pull_3", reps = 2, rpe = 8, time = 3),
            partial("pull_3", reps = 2, rpe = 8, time = 2),
            partial("pull_3", reps = 2, rpe = 8, time = 1)
        )

        val result = ProgressionEngine.evaluate("pull_3", sessions)

        assertEquals(CoachDecision.REGRESS_VARIATION, result.decision)
        assertEquals("pull_2", result.targetNode?.id)
    }

    @Test
    fun graphContainsCoreTracks() {
        val ids = SkillProgressionGraph.tracks.map { it.id }.toSet()

        assertEquals(true, "pull_up" in ids)
        assertEquals(true, "handstand" in ids)
        assertEquals(true, "muscle_up" in ids)
        assertEquals(true, "front_lever" in ids)
        assertEquals(true, "planche" in ids)
        assertNotNull(SkillProgressionGraph.node("pull_3"))
    }

    private fun mastered(node: String, reps: Int, rpe: Int, time: Long) =
        SessionPerformance(
            nodeId = node,
            samples = List(3) {
                PerformanceSample(MeasurementType.REPS, reps, rpe)
            },
            completedAt = time
        )

    private fun partial(node: String, reps: Int, rpe: Int, time: Long) =
        SessionPerformance(
            nodeId = node,
            samples = List(3) {
                PerformanceSample(MeasurementType.REPS, reps, rpe)
            },
            completedAt = time
        )
}

package com.rushd.calisthenicscoach.domain

import com.rushd.calisthenicscoach.data.AssessmentResultEntity
import org.junit.Assert.assertEquals
import org.junit.Test

class SkillStateEngineTest {

    @Test
    fun strongPullAssessmentStartsHigherInPullTrack() {
        val profile = UserProfile(
            equipment = setOf("Pull-up Bar"),
            goal = "Pull-up"
        )
        val results = listOf(
            AssessmentResultEntity(
                metricId = "pull_reps",
                value = 8.0,
                unit = "reps",
                goalContext = "Pull-up"
            )
        )

        val states = SkillStateEngine.initialStates(profile, results)
        val pull = states.first { it.skillId == "pull_up" }

        assertEquals("pull_explosive", pull.currentNodeId)
    }

    @Test
    fun beginnerAssessmentStartsAtFoundationNodes() {
        val profile = UserProfile()
        val states = SkillStateEngine.initialStates(profile, emptyList())

        assertEquals(10, states.size)
        assertEquals("pull_dead_hang", states.first { it.skillId == "pull_up" }.currentNodeId)
        assertEquals("push_incline", states.first { it.skillId == "push_up" }.currentNodeId)
        assertEquals("core_dead_bug", states.first { it.skillId == "core_hollow" }.currentNodeId)
        assertEquals("hs_pike", states.first { it.skillId == "handstand" }.currentNodeId)
        assertEquals("lsit_hollow", states.first { it.skillId == "l_sit" }.currentNodeId)
    }
}

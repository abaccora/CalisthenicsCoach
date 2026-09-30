package com.rushd.calisthenicscoach.domain

import com.rushd.calisthenicscoach.data.AssessmentResultEntity
import com.rushd.calisthenicscoach.data.SkillStateEntity

object SkillStateEngine {

    fun initialStates(
        profile: UserProfile,
        assessments: List<AssessmentResultEntity>
    ): List<SkillStateEntity> {
        val values = assessments.associate { it.metricId to it.value }

        val pullNode = when {
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 8.0 -> "pull_explosive"
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 3.0 -> "pull_regular"
            (values["hang_seconds"] ?: 0.0) >= 20.0 -> "pull_assisted"
            "Pull-up Bar" in profile.equipment -> "pull_dead_hang"
            else -> "pull_dead_hang"
        }

        val dipNode = when {
            (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 5.0 -> "dip_regular"
            else -> "dip_support"
        }

        val handstandNode = when {
            (values["pike_reps"] ?: 0.0) >= 8.0 -> "hs_shoulder_tap"
            else -> "hs_pike"
        }

        val lSitNode = "lsit_hollow"

        val muscleNode = when {
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 8.0 -> "mu_explosive"
            else -> "mu_pull_base"
        }

        return listOf(
            state("pull_up", pullNode),
            state("dip", dipNode),
            state("handstand", handstandNode),
            state("l_sit", lSitNode),
            state("muscle_up", muscleNode)
        )
    }

    fun nextNode(
        graph: SkillGraph,
        currentNodeId: String,
        successfulSessions: Int,
        latestRpe: Double?
    ): String {
        val node = graph.node(currentNodeId) ?: return currentNodeId
        return when {
            latestRpe != null && latestRpe >= 9.0 && node.regressionNodeId != null ->
                node.regressionNodeId
            successfulSessions >= node.masteryRule.successfulSessionsRequired &&
                latestRpe != null &&
                latestRpe <= node.masteryRule.maxRpe &&
                node.progressionNodeId != null ->
                node.progressionNodeId
            else -> currentNodeId
        }
    }

    private fun state(skillId: String, nodeId: String) = SkillStateEntity(
        skillId = skillId,
        currentNodeId = nodeId
    )
}

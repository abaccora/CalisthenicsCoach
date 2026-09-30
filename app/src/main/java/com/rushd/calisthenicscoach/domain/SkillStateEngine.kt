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
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 8.0 -> "pull_4"
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 3.0 -> "pull_3"
            (values["hang_seconds"] ?: 0.0) >= 20.0 -> "pull_2"
            else -> "pull_1"
        }

        val dipNode = when {
            (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 8.0 -> "dip_3"
            (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 3.0 -> "dip_2"
            else -> "dip_1"
        }

        val handstandNode = when {
            (values["pike_reps"] ?: 0.0) >= 10.0 -> "hs_3"
            (values["pike_reps"] ?: 0.0) >= 6.0 -> "hs_2"
            else -> "hs_1"
        }

        val lSitNode = when {
            (values["hollow_hold"] ?: profile.hollowHoldSec.toDouble()) >= 30.0 -> "ls_2"
            else -> "ls_1"
        }

        val muscleNode = when {
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 8.0 &&
                (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 8.0 -> "mu_3"
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 5.0 -> "mu_2"
            else -> "mu_1"
        }

        val frontLeverNode = if ((values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 6.0) "fl_2" else "fl_1"
        val plancheNode = if ((values["pike_reps"] ?: 0.0) >= 8.0) "pl_2" else "pl_1"
        val pistolNode = if ((values["split_squat_reps"] ?: 0.0) >= 10.0) "ps_2" else "ps_1"

        return listOf(
            state("pull_up", pullNode),
            state("dip", dipNode),
            state("handstand", handstandNode),
            state("l_sit", lSitNode),
            state("muscle_up", muscleNode),
            state("front_lever", frontLeverNode),
            state("planche", plancheNode),
            state("pistol_squat", pistolNode)
        )
    }

    fun nextNode(
        currentNodeId: String,
        successfulSessions: Int,
        latestRpe: Double?
    ): String {
        val current = SkillProgressionGraph.node(currentNodeId) ?: return currentNodeId
        return when {
            latestRpe != null && latestRpe >= 9.0 ->
                SkillProgressionGraph.previous(current.id)?.id ?: current.id
            successfulSessions >= current.mastery.requiredSuccessfulSessions &&
                latestRpe != null &&
                latestRpe <= current.mastery.maxRpe ->
                SkillProgressionGraph.next(current.id)?.id ?: current.id
            else -> current.id
        }
    }

    private fun state(skillId: String, nodeId: String) = SkillStateEntity(
        skillId = skillId,
        currentNodeId = nodeId
    )
}

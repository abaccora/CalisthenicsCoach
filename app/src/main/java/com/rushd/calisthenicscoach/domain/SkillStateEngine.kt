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
            (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 8.0 -> "dip_regular"
            (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 3.0 -> "dip_support"
            else -> "dip_bench"
        }

        val pushNode = when {
            (values["push_reps"] ?: profile.maxPushUps.toDouble()) >= 20.0 -> "push_deficit"
            (values["push_reps"] ?: profile.maxPushUps.toDouble()) >= 12.0 -> "push_decline"
            (values["push_reps"] ?: profile.maxPushUps.toDouble()) >= 6.0 -> "push_regular"
            else -> "push_incline"
        }

        val coreReps = values["core_reps"] ?: 0.0
        val coreNode = when {
            coreReps >= 15.0 -> "core_body_saw"
            coreReps >= 10.0 -> "core_hollow_hold"
            else -> "core_dead_bug"
        }

        val handstandNode = when {
            (values["pike_reps"] ?: 0.0) >= 10.0 -> "hs_wall"
            (values["pike_reps"] ?: 0.0) >= 6.0 -> "hs_shoulder_tap"
            else -> "hs_pike"
        }

        val lSitNode = when {
            coreReps >= 12.0 -> "lsit_compression"
            else -> "lsit_hollow"
        }

        val muscleNode = when {
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 8.0 &&
                (values["dip_reps"] ?: profile.maxDips.toDouble()) >= 8.0 -> "mu_explosive"
            (values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 5.0 -> "mu_dip_base"
            else -> "mu_pull_base"
        }

        val frontLeverNode =
            if ((values["pull_reps"] ?: profile.maxPullUps.toDouble()) >= 6.0) "fl_tuck" else "fl_body_saw"

        val plancheNode =
            if ((values["pike_reps"] ?: 0.0) >= 8.0) "pl_lean" else "pl_pseudo"

        val pistolNode =
            if ((values["split_squat_reps"] ?: 0.0) >= 10.0) "ps_bulgarian" else "ps_split"

        return listOf(
            state("pull_up", pullNode),
            state("dip", dipNode),
            state("push_up", pushNode),
            state("core_hollow", coreNode),
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
        val current = SkillGraphs.node(currentNodeId) ?: return currentNodeId

        return when {
            latestRpe != null && latestRpe >= 9.0 ->
                SkillGraphs.previous(current.id)?.id ?: current.id

            successfulSessions >= current.masteryRule.successfulSessionsRequired &&
                latestRpe != null &&
                latestRpe <= current.masteryRule.maxRpe ->
                SkillGraphs.next(current.id)?.id ?: current.id

            else -> current.id
        }
    }

    private fun state(skillId: String, nodeId: String) = SkillStateEntity(
        skillId = skillId,
        currentNodeId = nodeId
    )
}

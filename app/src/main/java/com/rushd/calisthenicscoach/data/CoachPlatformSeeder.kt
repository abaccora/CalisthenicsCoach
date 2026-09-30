package com.rushd.calisthenicscoach.data

import com.rushd.calisthenicscoach.data.source.HomeWorkoutSourceIndex
import com.rushd.calisthenicscoach.domain.ExerciseVideoCatalog
import com.rushd.calisthenicscoach.domain.MeasurementType
import com.rushd.calisthenicscoach.domain.SkillGraphs

/**
 * Seeds the Room-backed Coach Platform from the current curated catalog and
 * indexes the complete external Action library without exposing uncurated
 * records to plan generation.
 */
class CoachPlatformSeeder(
    private val dao: CoachPlatformDao
) {

    suspend fun seedFoundation() {
        seedSourceIndex()
        seedCuratedExercises()
        seedSkillGraphs()
    }

    suspend fun seedSourceIndex() {
        if (dao.countSourceActions(HomeWorkoutSourceIndex.SOURCE) !=
            HomeWorkoutSourceIndex.EXPECTED_ACTION_COUNT
        ) {
            dao.upsertSourceActions(HomeWorkoutSourceIndex.asEntities())
        }
    }

    private suspend fun seedCuratedExercises() {
        val fromVideoCatalog = ExerciseVideoCatalog.all.map { item ->
            ExerciseEntity(
                id = item.key,
                technicalName = item.nameEn,
                nameEn = item.nameEn,
                nameAr = item.nameAr,
                movementPattern = movementPattern(item.key, item.category),
                difficulty = difficulty(item.key),
                measurementType = MeasurementType.REPS.name,
                primaryMusclesCsv = muscles(item.category),
                equipmentCsv = equipment(item.key),
                videoKey = item.key,
                isCoreCatalog = true,
                source = "CURATED"
            )
        }

        val knownNames = fromVideoCatalog.map { it.technicalName.lowercase() }.toSet()
        val progressionOnly = SkillGraphs.all
            .flatMap { it.nodes }
            .mapNotNull { node ->
                val technicalName = node.exerciseName ?: node.titleEn
                if (technicalName.lowercase() in knownNames) return@mapNotNull null
                ExerciseEntity(
                    id = "skill_${slug(technicalName)}",
                    technicalName = technicalName,
                    nameEn = node.titleEn,
                    nameAr = node.titleAr,
                    movementPattern = skillPattern(node.skillId),
                    difficulty = when {
                        node.level <= 1 -> "BEGINNER"
                        node.level <= 3 -> "INTERMEDIATE"
                        else -> "ADVANCED"
                    },
                    measurementType = node.measurementType.name,
                    equipmentCsv = node.equipment.joinToString(","),
                    isCoreCatalog = true,
                    source = "CURATED_SKILL"
                )
            }
            .distinctBy { it.id }

        dao.upsertExercises((fromVideoCatalog + progressionOnly).distinctBy { it.id })
    }

    private suspend fun seedSkillGraphs() {
        val exercises = ExerciseVideoCatalog.all.associateBy { it.nameEn.lowercase() }

        val skills = SkillGraphs.all.mapIndexed { index, graph ->
            SkillEntity(
                id = graph.skillId,
                nameEn = graph.titleEn,
                nameAr = graph.titleAr,
                displayOrder = index
            )
        }
        dao.upsertSkills(skills)

        val nodes = SkillGraphs.all.flatMap { graph ->
            graph.nodes.map { node ->
                val catalogItem = node.exerciseName?.let { exercises[it.lowercase()] }
                val exerciseId = catalogItem?.key ?: "skill_${slug(node.exerciseName ?: node.titleEn)}"
                ProgressionNodeEntity(
                    id = node.id,
                    skillId = graph.skillId,
                    exerciseId = exerciseId,
                    orderIndex = node.level,
                    entryRuleJson = prerequisitesJson(node.prerequisites),
                    masteryRuleJson = masteryJson(
                        sets = node.masteryRule.minSets,
                        target = node.masteryRule.targetValue,
                        maxRpe = node.masteryRule.maxRpe,
                        sessions = node.masteryRule.successfulSessionsRequired,
                        measurement = node.measurementType.name
                    ),
                    minimumSessions = node.masteryRule.successfulSessionsRequired
                )
            }
        }
        dao.upsertProgressionNodes(nodes)

        val edges = SkillGraphs.all.flatMap { graph ->
            graph.nodes
                .sortedBy { it.level }
                .zipWithNext { from, to ->
                    ProgressionEdgeEntity(
                        fromNodeId = from.id,
                        toNodeId = to.id,
                        relation = "PROGRESSION",
                        priority = 100
                    )
                }
        }
        dao.upsertProgressionEdges(edges)
    }

    private fun movementPattern(key: String, category: String): String = when {
        "pull_up" in key -> "VERTICAL_PULL"
        key == "inverted_row" -> "HORIZONTAL_PULL"
        "dip" in key -> "VERTICAL_PUSH"
        "push_up" in key -> "HORIZONTAL_PUSH"
        key in setOf("air_squat", "sumo_squat", "split_squat", "bulgarian_split_squat") -> "SQUAT"
        key in setOf("single_leg_deadlift", "hip_thrust", "reverse_hyperextension") -> "HIP_HINGE"
        category == "Core" -> "CORE"
        category == "Mobility" -> "MOBILITY"
        category == "Cardio" -> "CONDITIONING"
        category == "Back" -> "PULL"
        category == "Chest" || category == "Arms & Shoulders" -> "PUSH"
        category == "Legs" -> "LOWER_BODY"
        else -> "GENERAL"
    }

    private fun skillPattern(skillId: String): String = when (skillId) {
        "pull_up", "muscle_up", "front_lever" -> "VERTICAL_PULL"
        "dip", "planche", "handstand", "push_up" -> "PUSH"
        "l_sit" -> "CORE_COMPRESSION"
        "core_hollow" -> "CORE"
        "pistol_squat" -> "UNILATERAL_SQUAT"
        else -> "GENERAL"
    }

    private fun difficulty(key: String): String = when {
        key.startsWith("regular_") || key in setOf("chest_dip", "parallel_grip_pull_up") -> "INTERMEDIATE"
        key in setOf("deficit_push_up", "pseudo_push_up", "body_saw") -> "INTERMEDIATE"
        else -> "BEGINNER"
    }

    private fun equipment(key: String): String = when {
        "pull_up" in key -> "Pull-up Bar"
        "dip" in key && key != "bench_dip" -> "Parallel Bars"
        key == "bench_dip" || key.startsWith("bench_") -> "Bench"
        else -> ""
    }

    private fun muscles(category: String): String = when (category) {
        "Back" -> "Latissimus Dorsi,Biceps,Core"
        "Chest" -> "Pectoralis,Triceps,Anterior Deltoid"
        "Arms & Shoulders" -> "Deltoids,Triceps,Core"
        "Legs" -> "Quadriceps,Glutes,Hamstrings"
        "Core" -> "Rectus Abdominis,Obliques,Deep Core"
        else -> ""
    }

    private fun masteryJson(
        sets: Int,
        target: Int,
        maxRpe: Int,
        sessions: Int,
        measurement: String
    ): String =
        """{"sets":$sets,"target":$target,"maxRpe":$maxRpe,"sessions":$sessions,"measurement":"$measurement"}"""

    private fun prerequisitesJson(ids: Set<String>): String =
        if (ids.isEmpty()) {
            """{"nodes":[]}"""
        } else {
            ids.joinToString(prefix = """{"nodes":[""", postfix = """]}""", separator = """,""") { it }
        }

    private fun slug(value: String): String =
        value.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')
}

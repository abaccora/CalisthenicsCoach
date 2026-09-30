package com.rushd.calisthenicscoach.domain

data class ExerciseSubstitute(
    val video: ExerciseVideo,
    val score: Int,
    val reasonAr: String
)

object ExerciseSubstitutionEngine {

    fun alternatives(
        currentExerciseName: String,
        availableEquipment: Set<String> = emptySet(),
        limit: Int = 8
    ): List<ExerciseSubstitute> {
        val currentVideo = ExerciseVideoCatalog.forExercise(currentExerciseName)
        val currentNodes = SkillGraphs.all
            .flatMap { graph -> graph.nodes }
            .filter { it.exerciseName == currentExerciseName }
        val currentSkillIds = currentNodes.map { it.skillId }.toSet()
        val currentLevel = currentNodes.minOfOrNull { it.level }

        return ExerciseVideoCatalog.all
            .asSequence()
            .filter { it.nameEn != currentExerciseName }
            .map { candidate ->
                val candidateNodes = SkillGraphs.all
                    .flatMap { graph -> graph.nodes }
                    .filter { it.exerciseName == candidate.nameEn }

                val sameTrack = candidateNodes.any { it.skillId in currentSkillIds }
                val equipmentAllowed = candidateNodes.all { node ->
                    node.equipment.isEmpty() || node.equipment.all { it in availableEquipment }
                }
                val sameCategory = currentVideo != null && candidate.category == currentVideo.category
                val levelDistance = if (currentLevel != null) {
                    candidateNodes.minOfOrNull { kotlin.math.abs(it.level - currentLevel) } ?: 99
                } else 99

                val score =
                    (if (sameTrack) 100 else 0) +
                    (if (sameCategory) 30 else 0) +
                    (if (equipmentAllowed) 20 else -80) +
                    (if (levelDistance <= 1) 15 else 0)

                val reason = when {
                    sameTrack && levelDistance == 0 -> "بديل من نفس مسار المهارة وبالمستوى نفسه"
                    sameTrack && levelDistance <= 1 -> "بديل قريب داخل مسار المهارة"
                    sameTrack -> "بديل من مسار المهارة نفسه"
                    sameCategory -> "بديل من المجموعة الحركية نفسها"
                    else -> "بديل متاح"
                }

                ExerciseSubstitute(candidate, score, reason)
            }
            .filter { it.score > 0 }
            .sortedByDescending { it.score }
            .take(limit)
            .toList()
    }
}

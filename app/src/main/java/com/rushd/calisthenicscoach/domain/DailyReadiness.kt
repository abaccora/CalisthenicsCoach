package com.rushd.calisthenicscoach.domain

enum class EnergyLevel {
    HIGH,
    NORMAL,
    LOW
}

data class DailyReadiness(
    val energy: EnergyLevel = EnergyLevel.NORMAL,
    val minutesAvailable: Int,
    val equipmentAvailable: Set<String>
)

object DailySessionAdapter {

    fun adapt(
        plan: WorkoutPlan,
        readiness: DailyReadiness
    ): WorkoutPlan {
        val adjustedExercises = plan.exercises.map { exercise ->
            val required = requiredEquipment(exercise.name)
            val equipmentMissing = required.isNotEmpty() &&
                !required.all { it in readiness.equipmentAvailable }

            val resolved = if (equipmentMissing) {
                val replacement = ExerciseSubstitutionEngine.alternatives(
                    currentExerciseName = exercise.name,
                    availableEquipment = readiness.equipmentAvailable,
                    limit = 1
                ).firstOrNull()?.video

                if (replacement != null) {
                    exercise.copy(
                        name = replacement.nameEn,
                        ArabicName = replacement.nameAr
                    )
                } else exercise
            } else exercise

            when (readiness.energy) {
                EnergyLevel.HIGH -> resolved
                EnergyLevel.NORMAL -> resolved
                EnergyLevel.LOW -> resolved.copy(
                    sets = (resolved.sets - 1).coerceAtLeast(2),
                    restSec = (resolved.restSec + 15).coerceAtMost(180)
                )
            }
        }

        val durationRatio = readiness.minutesAvailable.toDouble() /
            plan.durationMin.coerceAtLeast(1).toDouble()

        val exerciseCount = when {
            durationRatio >= 0.9 -> adjustedExercises.size
            durationRatio >= 0.65 -> (adjustedExercises.size - 1).coerceAtLeast(2)
            else -> (adjustedExercises.size * durationRatio)
                .toInt()
                .coerceIn(2, adjustedExercises.size)
        }

        return plan.copy(
            id = plan.id,
            durationMin = readiness.minutesAvailable,
            subtitle = when {
                readiness.minutesAvailable < plan.durationMin ->
                    "${plan.subtitle} · نسخة مختصرة لليوم"
                readiness.energy == EnergyLevel.LOW ->
                    "${plan.subtitle} · حمل أخف لليوم"
                else -> plan.subtitle
            },
            exercises = adjustedExercises.take(exerciseCount)
        )
    }

    private fun requiredEquipment(exerciseName: String): Set<String> {
        val fromGraph = SkillGraphs.all
            .flatMap { it.nodes }
            .firstOrNull { it.exerciseName == exerciseName }
            ?.equipment
            .orEmpty()

        if (fromGraph.isNotEmpty()) return fromGraph

        return when (exerciseName) {
            "Regular Pull Up",
            "Parallel Grip Pull Up",
            "Scapular Pull Up",
            "Passive Hang" -> setOf("Pull-up Bar")
            "Chest Dip" -> setOf("Parallel Bars")
            "Bench Dip" -> setOf("Bench")
            else -> emptySet()
        }
    }
}

package com.rushd.calisthenicscoach.domain

data class AssessmentMetric(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val unit: String,
    val measurementType: MeasurementType,
    val exerciseName: String? = null,
    val optional: Boolean = false
)

data class AssessmentPlan(
    val titleAr: String,
    val goal: String,
    val metrics: List<AssessmentMetric>
)

object AssessmentEngine {

    fun build(profile: UserProfile): AssessmentPlan {
        val base = mutableListOf(
            AssessmentMetric(
                id = "push_reps",
                titleAr = "أقصى عدد من تمارين الضغط بتقنية سليمة",
                titleEn = "Max Push-ups",
                unit = "reps",
                measurementType = MeasurementType.REPS,
                exerciseName = "Regular Push Up"
            ),
            AssessmentMetric(
                id = "core_reps",
                titleAr = "تمرين الجذع المتعاكس لكل جهة",
                titleEn = "Dead Bug",
                unit = "reps_per_side",
                measurementType = MeasurementType.LEFT_RIGHT_REPS,
                exerciseName = "Dead Bug"
            ),
            AssessmentMetric(
                id = "split_squat_reps",
                titleAr = "قرفصاء بوضعية الاندفاع الثابت لكل جهة",
                titleEn = "Split Squat",
                unit = "reps_per_side",
                measurementType = MeasurementType.UNILATERAL_REPS,
                exerciseName = "Split Squat"
            )
        )

        if ("Pull-up Bar" in profile.equipment) {
            base += AssessmentMetric(
                id = "pull_reps",
                titleAr = "أقصى عدد من السحب على البار بتقنية سليمة",
                titleEn = "Max Pull-ups",
                unit = "reps",
                measurementType = MeasurementType.REPS,
                exerciseName = "Regular Pull Up",
                optional = true
            )
        }

        if ("Parallel Bars" in profile.equipment) {
            base += AssessmentMetric(
                id = "dip_reps",
                titleAr = "أقصى عدد من الضغط على المتوازي بتقنية سليمة",
                titleEn = "Max Dips",
                unit = "reps",
                measurementType = MeasurementType.REPS,
                exerciseName = "Chest Dip",
                optional = true
            )
        }

        when (profile.goal) {
            "Handstand", "Planche" -> {
                base += AssessmentMetric(
                    id = "pike_reps",
                    titleAr = "ضغط الكتفين بوضعية V مقلوبة",
                    titleEn = "Pike Push-up",
                    unit = "reps",
                    measurementType = MeasurementType.REPS,
                    exerciseName = "Pike Push Up"
                )
            }
            "Pull-up", "Muscle-up", "Front Lever" -> {
                if ("Pull-up Bar" !in profile.equipment) {
                    base += AssessmentMetric(
                        id = "back_control",
                        titleAr = "رفع الذراع والساق المتعاكسين لكل جهة",
                        titleEn = "Alternating Superman",
                        unit = "reps_per_side",
                        measurementType = MeasurementType.UNILATERAL_REPS,
                        exerciseName = "Alternating Superman"
                    )
                }
            }
        }

        val playable = base.filter { metric ->
            metric.exerciseName?.let(ExerciseVideoCatalog::forExercise) != null
        }

        return AssessmentPlan(
            titleAr = "تقييم البداية",
            goal = profile.goal,
            metrics = playable
        )
    }
}

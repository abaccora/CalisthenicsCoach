package com.rushd.calisthenicscoach.domain

enum class MeasurementType {
    REPS,
    HOLD_SECONDS,
    UNILATERAL_REPS
}

data class MasteryRule(
    val minSets: Int,
    val targetValue: Int,
    val maxRpe: Int = 8,
    val successfulSessionsRequired: Int = 2
)

data class ProgressionNode(
    val id: String,
    val skillId: String,
    val exerciseName: String,
    val titleAr: String,
    val titleEn: String,
    val level: Int,
    val measurementType: MeasurementType,
    val equipment: Set<String> = emptySet(),
    val masteryRule: MasteryRule,
    val regressionNodeId: String? = null,
    val progressionNodeId: String? = null
)

data class SkillGraph(
    val skillId: String,
    val titleAr: String,
    val titleEn: String,
    val nodes: List<ProgressionNode>
) {
    fun node(id: String): ProgressionNode? = nodes.firstOrNull { it.id == id }
}

object SkillGraphs {

    val pullUp = SkillGraph(
        skillId = "pull_up",
        titleAr = "العقلة",
        titleEn = "Pull-up",
        nodes = listOf(
            ProgressionNode(
                id = "pull_dead_hang",
                skillId = "pull_up",
                exerciseName = "Passive Hang",
                titleAr = "التعلق الحر",
                titleEn = "Dead Hang",
                level = 1,
                measurementType = MeasurementType.HOLD_SECONDS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(3, 30),
                progressionNodeId = "pull_scapular"
            ),
            ProgressionNode(
                id = "pull_scapular",
                skillId = "pull_up",
                exerciseName = "Scapular Pull Up",
                titleAr = "سحب لوح الكتف",
                titleEn = "Scapular Pull-up",
                level = 2,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(3, 10),
                regressionNodeId = "pull_dead_hang",
                progressionNodeId = "pull_assisted"
            ),
            ProgressionNode(
                id = "pull_assisted",
                skillId = "pull_up",
                exerciseName = "Parallel Grip Pull Up",
                titleAr = "عقلة بمساعدة",
                titleEn = "Assisted Pull-up",
                level = 3,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(3, 8),
                regressionNodeId = "pull_scapular",
                progressionNodeId = "pull_regular"
            ),
            ProgressionNode(
                id = "pull_regular",
                skillId = "pull_up",
                exerciseName = "Regular Pull Up",
                titleAr = "العقلة",
                titleEn = "Pull-up",
                level = 4,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(3, 8),
                regressionNodeId = "pull_assisted",
                progressionNodeId = "pull_explosive"
            ),
            ProgressionNode(
                id = "pull_explosive",
                skillId = "pull_up",
                exerciseName = "Regular Pull Up",
                titleAr = "العقلة الانفجارية",
                titleEn = "Explosive Pull-up",
                level = 5,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(4, 5),
                regressionNodeId = "pull_regular"
            )
        )
    )

    val dip = SkillGraph(
        skillId = "dip",
        titleAr = "المتوازي",
        titleEn = "Dip",
        nodes = listOf(
            ProgressionNode(
                id = "dip_support",
                skillId = "dip",
                exerciseName = "Parallel Bar Support Hold",
                titleAr = "الثبات على المتوازي",
                titleEn = "Support Hold",
                level = 1,
                measurementType = MeasurementType.HOLD_SECONDS,
                equipment = setOf("Parallel Bars"),
                masteryRule = MasteryRule(3, 30),
                progressionNodeId = "dip_regular"
            ),
            ProgressionNode(
                id = "dip_regular",
                skillId = "dip",
                exerciseName = "Chest Dip",
                titleAr = "المتوازي",
                titleEn = "Dip",
                level = 2,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Parallel Bars"),
                masteryRule = MasteryRule(3, 10),
                regressionNodeId = "dip_support"
            )
        )
    )

    val handstand = SkillGraph(
        skillId = "handstand",
        titleAr = "الوقوف على اليدين",
        titleEn = "Handstand",
        nodes = listOf(
            ProgressionNode(
                id = "hs_pike",
                skillId = "handstand",
                exerciseName = "Pike Push Up",
                titleAr = "ضغط بايك",
                titleEn = "Pike Push-up",
                level = 1,
                measurementType = MeasurementType.REPS,
                masteryRule = MasteryRule(3, 10),
                progressionNodeId = "hs_shoulder_tap"
            ),
            ProgressionNode(
                id = "hs_shoulder_tap",
                skillId = "handstand",
                exerciseName = "Plank Shoulder Tap",
                titleAr = "لمس الكتف من البلانك",
                titleEn = "Shoulder Tap",
                level = 2,
                measurementType = MeasurementType.UNILATERAL_REPS,
                masteryRule = MasteryRule(3, 10),
                regressionNodeId = "hs_pike"
            )
        )
    )

    val lSit = SkillGraph(
        skillId = "l_sit",
        titleAr = "إل-سِت",
        titleEn = "L-sit",
        nodes = listOf(
            ProgressionNode(
                id = "lsit_hollow",
                skillId = "l_sit",
                exerciseName = "Hollow Body Hold",
                titleAr = "الثبات المجوف",
                titleEn = "Hollow Hold",
                level = 1,
                measurementType = MeasurementType.HOLD_SECONDS,
                masteryRule = MasteryRule(3, 30)
            )
        )
    )

    val muscleUp = SkillGraph(
        skillId = "muscle_up",
        titleAr = "المسل أب",
        titleEn = "Muscle-up",
        nodes = listOf(
            ProgressionNode(
                id = "mu_pull_base",
                skillId = "muscle_up",
                exerciseName = "Regular Pull Up",
                titleAr = "قاعدة السحب",
                titleEn = "Pull-up Base",
                level = 1,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(4, 8),
                progressionNodeId = "mu_explosive"
            ),
            ProgressionNode(
                id = "mu_explosive",
                skillId = "muscle_up",
                exerciseName = "Regular Pull Up",
                titleAr = "السحب الانفجاري",
                titleEn = "Explosive Pull-up",
                level = 2,
                measurementType = MeasurementType.REPS,
                equipment = setOf("Pull-up Bar"),
                masteryRule = MasteryRule(4, 5),
                regressionNodeId = "mu_pull_base"
            )
        )
    )

    val all = listOf(pullUp, dip, handstand, lSit, muscleUp)

    fun byId(skillId: String): SkillGraph? = all.firstOrNull { it.skillId == skillId }
}

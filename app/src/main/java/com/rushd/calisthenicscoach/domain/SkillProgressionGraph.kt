package com.rushd.calisthenicscoach.domain

enum class MeasurementType {
    REPS,
    HOLD_SECONDS,
    LEFT_RIGHT_REPS,
    UNILATERAL_REPS,
    QUALITY_REPS
}

data class MasteryRule(
    val measurement: MeasurementType,
    val target: Int,
    val sets: Int,
    val maxRpe: Int = 8,
    val requiredSuccessfulSessions: Int = 2
)

data class ProgressionNode(
    val id: String,
    val skillId: String,
    val titleAr: String,
    val titleEn: String,
    val exerciseName: String?,
    val level: Int,
    val equipment: Set<String> = emptySet(),
    val mastery: MasteryRule,
    val prerequisites: Set<String> = emptySet(),
    val notesAr: String = ""
)

data class SkillTrack(
    val id: String,
    val titleAr: String,
    val titleEn: String,
    val nodes: List<ProgressionNode>
)

object SkillProgressionGraph {

    val tracks: List<SkillTrack> = listOf(
        pullUpTrack(),
        dipTrack(),
        handstandTrack(),
        lSitTrack(),
        muscleUpTrack(),
        frontLeverTrack(),
        plancheTrack(),
        pistolSquatTrack()
    )

    val nodes: Map<String, ProgressionNode> =
        tracks.flatMap { it.nodes }.associateBy { it.id }

    fun track(skillId: String): SkillTrack? = tracks.firstOrNull { it.id == skillId }

    fun node(nodeId: String): ProgressionNode? = nodes[nodeId]

    fun next(nodeId: String): ProgressionNode? {
        val current = nodes[nodeId] ?: return null
        return track(current.skillId)
            ?.nodes
            ?.sortedBy { it.level }
            ?.firstOrNull { it.level > current.level }
    }

    fun previous(nodeId: String): ProgressionNode? {
        val current = nodes[nodeId] ?: return null
        return track(current.skillId)
            ?.nodes
            ?.sortedByDescending { it.level }
            ?.firstOrNull { it.level < current.level }
    }

    private fun pullUpTrack() = SkillTrack(
        id = "pull_up",
        titleAr = "العقلة",
        titleEn = "Pull-up",
        nodes = listOf(
            node("pull_1", "pull_up", "السحب الأفقي", "Inverted Row", "Inverted Row", 1, rule(10, 3)),
            node("pull_2", "pull_up", "عقلة بقبضة متوازية", "Parallel Grip Pull Up", "Parallel Grip Pull Up", 2, rule(6, 3), setOf("pull_1"), setOf("Pull-up Bar")),
            node("pull_3", "pull_up", "العقلة", "Pull-up", "Regular Pull Up", 3, rule(8, 3), setOf("pull_2"), setOf("Pull-up Bar")),
            node("pull_4", "pull_up", "عقلة عالية", "High Pull-up", null, 4, rule(5, 3), setOf("pull_3"), setOf("Pull-up Bar")),
            node("pull_5", "pull_up", "عقلة انفجارية", "Explosive Pull-up", null, 5, rule(5, 3), setOf("pull_4"), setOf("Pull-up Bar"))
        )
    )

    private fun dipTrack() = SkillTrack(
        id = "dip",
        titleAr = "المتوازي",
        titleEn = "Dip",
        nodes = listOf(
            node("dip_1", "dip", "ديبس على مقعد", "Bench Dip", "Bench Dip", 1, rule(12, 3)),
            node("dip_2", "dip", "ديبس للصدر", "Chest Dip", "Chest Dip", 2, rule(8, 3), setOf("dip_1"), setOf("Parallel Bars")),
            node("dip_3", "dip", "ديبس عميق", "Deep Dip", null, 3, rule(8, 3), setOf("dip_2"), setOf("Parallel Bars")),
            node("dip_4", "dip", "ديبس انفجاري", "Explosive Dip", null, 4, rule(6, 3), setOf("dip_3"), setOf("Parallel Bars"))
        )
    )

    private fun handstandTrack() = SkillTrack(
        id = "handstand",
        titleAr = "الوقوف على اليدين",
        titleEn = "Handstand",
        nodes = listOf(
            node("hs_1", "handstand", "ضغط بايك", "Pike Push Up", "Pike Push Up", 1, rule(10, 3)),
            node("hs_2", "handstand", "لمس الكتف من البلانك", "Plank Shoulder Tap", "Plank Shoulder Tap", 2, rule(10, 3), setOf("hs_1")),
            node("hs_3", "handstand", "وقوف على اليدين بمساعدة الحائط", "Wall Handstand", null, 3, holdRule(30, 3), setOf("hs_2")),
            node("hs_4", "handstand", "وقوف على اليدين مع انفصال قصير عن الحائط", "Wall Toe Pull", null, 4, holdRule(20, 3), setOf("hs_3")),
            node("hs_5", "handstand", "وقوف حر على اليدين", "Freestanding Handstand", null, 5, holdRule(20, 3), setOf("hs_4"))
        )
    )

    private fun lSitTrack() = SkillTrack(
        id = "l_sit",
        titleAr = "إل-سِت",
        titleEn = "L-sit",
        nodes = listOf(
            node("ls_1", "l_sit", "كرنش عكسي", "Reverse Crunch", "Reverse Crunch", 1, rule(15, 3)),
            node("ls_2", "l_sit", "جلوس مضغوط مع رفع القدمين", "Seated Compression Lift", null, 2, rule(10, 3), setOf("ls_1")),
            node("ls_3", "l_sit", "تَك إل-سِت", "Tuck L-sit", null, 3, holdRule(20, 3), setOf("ls_2")),
            node("ls_4", "l_sit", "إل-سِت بساق واحدة", "One-leg L-sit", null, 4, holdRule(15, 3), setOf("ls_3")),
            node("ls_5", "l_sit", "إل-سِت كامل", "L-sit", null, 5, holdRule(20, 3), setOf("ls_4"))
        )
    )

    private fun muscleUpTrack() = SkillTrack(
        id = "muscle_up",
        titleAr = "المَسِل أب",
        titleEn = "Muscle-up",
        nodes = listOf(
            node("mu_1", "muscle_up", "العقلة", "Pull-up", "Regular Pull Up", 1, rule(8, 3), equipment = setOf("Pull-up Bar")),
            node("mu_2", "muscle_up", "ديبس للصدر", "Chest Dip", "Chest Dip", 2, rule(10, 3), setOf("mu_1"), setOf("Parallel Bars")),
            node("mu_3", "muscle_up", "عقلة انفجارية إلى الصدر", "Explosive Chest Pull-up", null, 3, rule(5, 3), setOf("mu_2"), setOf("Pull-up Bar")),
            node("mu_4", "muscle_up", "انتقال منخفض للمَسِل أب", "Low Muscle-up Transition", null, 4, rule(5, 3), setOf("mu_3"), setOf("Pull-up Bar")),
            node("mu_5", "muscle_up", "مَسِل أب", "Muscle-up", null, 5, rule(3, 3), setOf("mu_4"), setOf("Pull-up Bar"))
        )
    )

    private fun frontLeverTrack() = SkillTrack(
        id = "front_lever",
        titleAr = "الفرونت ليفر",
        titleEn = "Front Lever",
        nodes = listOf(
            node("fl_1", "front_lever", "بودي سو", "Body Saw", "Body Saw", 1, rule(12, 3)),
            node("fl_2", "front_lever", "تَك فرونت ليفر", "Tuck Front Lever", null, 2, holdRule(15, 3), setOf("fl_1"), setOf("Pull-up Bar")),
            node("fl_3", "front_lever", "تَك متقدم", "Advanced Tuck Front Lever", null, 3, holdRule(12, 3), setOf("fl_2"), setOf("Pull-up Bar")),
            node("fl_4", "front_lever", "فرونت ليفر بساق واحدة", "One-leg Front Lever", null, 4, holdRule(10, 3), setOf("fl_3"), setOf("Pull-up Bar")),
            node("fl_5", "front_lever", "فرونت ليفر كامل", "Front Lever", null, 5, holdRule(10, 3), setOf("fl_4"), setOf("Pull-up Bar"))
        )
    )

    private fun plancheTrack() = SkillTrack(
        id = "planche",
        titleAr = "البلانش",
        titleEn = "Planche",
        nodes = listOf(
            node("pl_1", "planche", "ضغط سودو", "Pseudo Push Up", "Pseudo Push Up", 1, rule(10, 3)),
            node("pl_2", "planche", "ميل البلانش", "Planche Lean", null, 2, holdRule(25, 3), setOf("pl_1")),
            node("pl_3", "planche", "تَك بلانش", "Tuck Planche", null, 3, holdRule(12, 3), setOf("pl_2")),
            node("pl_4", "planche", "تَك بلانش متقدم", "Advanced Tuck Planche", null, 4, holdRule(10, 3), setOf("pl_3")),
            node("pl_5", "planche", "بلانش كامل", "Full Planche", null, 5, holdRule(8, 3), setOf("pl_4"))
        )
    )

    private fun pistolSquatTrack() = SkillTrack(
        id = "pistol_squat",
        titleAr = "القرفصاء بساق واحدة",
        titleEn = "Pistol Squat",
        nodes = listOf(
            node("ps_1", "pistol_squat", "قرفصاء منفصلة", "Split Squat", "Split Squat", 1, sideRule(10, 3)),
            node("ps_2", "pistol_squat", "قرفصاء بلغارية", "Bulgarian Split Squat", "Bulgarian Split Squat", 2, sideRule(10, 3), setOf("ps_1")),
            node("ps_3", "pistol_squat", "قرفصاء بساق واحدة إلى مقعد", "Box Pistol Squat", null, 3, sideRule(8, 3), setOf("ps_2")),
            node("ps_4", "pistol_squat", "قرفصاء بساق واحدة بمساعدة", "Assisted Pistol Squat", null, 4, sideRule(8, 3), setOf("ps_3")),
            node("ps_5", "pistol_squat", "قرفصاء كاملة بساق واحدة", "Pistol Squat", null, 5, sideRule(6, 3), setOf("ps_4"))
        )
    )

    private fun node(
        id: String,
        skill: String,
        ar: String,
        en: String,
        exercise: String?,
        level: Int,
        mastery: MasteryRule,
        prerequisites: Set<String> = emptySet(),
        equipment: Set<String> = emptySet()
    ) = ProgressionNode(
        id = id,
        skillId = skill,
        titleAr = ar,
        titleEn = en,
        exerciseName = exercise,
        level = level,
        equipment = equipment,
        mastery = mastery,
        prerequisites = prerequisites
    )

    private fun rule(target: Int, sets: Int) =
        MasteryRule(MeasurementType.REPS, target, sets)

    private fun holdRule(targetSeconds: Int, sets: Int) =
        MasteryRule(MeasurementType.HOLD_SECONDS, targetSeconds, sets)

    private fun sideRule(target: Int, sets: Int) =
        MasteryRule(MeasurementType.LEFT_RIGHT_REPS, target, sets)
}

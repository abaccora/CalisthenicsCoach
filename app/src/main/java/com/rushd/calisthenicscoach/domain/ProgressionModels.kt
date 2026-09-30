package com.rushd.calisthenicscoach.domain

enum class MeasurementType {
    REPS,
    HOLD_SECONDS,
    UNILATERAL_REPS,
    LEFT_RIGHT_REPS,
    QUALITY_REPS
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
    val exerciseName: String?,
    val titleAr: String,
    val titleEn: String,
    val level: Int,
    val measurementType: MeasurementType,
    val equipment: Set<String> = emptySet(),
    val masteryRule: MasteryRule,
    val prerequisites: Set<String> = emptySet()
)

data class SkillGraph(
    val skillId: String,
    val titleAr: String,
    val titleEn: String,
    val nodes: List<ProgressionNode>
)

object SkillGraphs {

    val all: List<SkillGraph> = listOf(
        pullUp(),
        dip(),
        handstand(),
        lSit(),
        muscleUp(),
        frontLever(),
        planche(),
        pistolSquat()
    )

    private val nodesById = all.flatMap { it.nodes }.associateBy { it.id }

    fun byId(skillId: String): SkillGraph? = all.firstOrNull { it.skillId == skillId }
    fun track(skillId: String): SkillGraph? = byId(skillId)
    fun node(nodeId: String): ProgressionNode? = nodesById[nodeId]

    fun next(nodeId: String): ProgressionNode? {
        val current = node(nodeId) ?: return null
        return byId(current.skillId)
            ?.nodes
            ?.filter { it.level > current.level }
            ?.minByOrNull { it.level }
    }

    fun previous(nodeId: String): ProgressionNode? {
        val current = node(nodeId) ?: return null
        return byId(current.skillId)
            ?.nodes
            ?.filter { it.level < current.level }
            ?.maxByOrNull { it.level }
    }

    private fun pullUp() = SkillGraph(
        "pull_up", "السحب على البار", "Pull-up",
        listOf(
            n("pull_dead_hang","pull_up","Passive Hang","التعلّق بالبار","Dead Hang",1,MeasurementType.HOLD_SECONDS,3,30,setOf("Pull-up Bar")),
            n("pull_assisted","pull_up","Parallel Grip Pull Up","سحب على البار بمساعدة","Assisted Pull-up",2,MeasurementType.REPS,3,8,setOf("Pull-up Bar")),
            n("pull_regular","pull_up","Regular Pull Up","السحب على البار","Pull-up",3,MeasurementType.REPS,3,8,setOf("Pull-up Bar")),
            n("pull_explosive","pull_up","Regular Pull Up","السحب على البار الانفجارية","Explosive Pull-up",4,MeasurementType.REPS,4,5,setOf("Pull-up Bar"))
        )
    )

    private fun dip() = SkillGraph(
        "dip", "الضغط على المتوازي", "Dip",
        listOf(
            n("dip_bench","dip","Bench Dip","ضغط خلفي على المقعد","Bench Dip",1,MeasurementType.REPS,3,12,setOf("Bench")),
            n("dip_support","dip","Chest Dip","الثبات والتحكم على الضغط على المتوازي","Dip Support",2,MeasurementType.REPS,3,5,setOf("Parallel Bars")),
            n("dip_regular","dip","Chest Dip","الضغط على المتوازي","Dip",3,MeasurementType.REPS,3,10,setOf("Parallel Bars"))
        )
    )

    private fun handstand() = SkillGraph(
        "handstand", "الوقوف على اليدين", "Handstand",
        listOf(
            n("hs_pike","handstand","Pike Push Up","ضغط الكتفين بوضعية V مقلوبة","Pike Push-up",1,MeasurementType.REPS,3,10),
            n("hs_shoulder_tap","handstand","Plank Shoulder Tap","لمس الكتفين من وضعية البلانك","Shoulder Tap",2,MeasurementType.UNILATERAL_REPS,3,10),
            n("hs_wall","handstand","Pike Push Up","الوقوف على اليدين بمساعدة الحائط","Wall Handstand Prep",3,MeasurementType.HOLD_SECONDS,3,30),
            n("hs_free","handstand","Pike Push Up","الوقوف الحر على اليدين","Freestanding Handstand",4,MeasurementType.HOLD_SECONDS,3,20)
        )
    )

    private fun lSit() = SkillGraph(
        "l_sit", "الثبات في وضعية L (L-sit)", "L-sit",
        listOf(
            n("lsit_hollow","l_sit","Hollow Body Hold","الثبات المجوف","Hollow Hold",1,MeasurementType.HOLD_SECONDS,3,30),
            n("lsit_compression","l_sit","Reverse Crunch","ضغط الجذع ورفع الحوض","Compression",2,MeasurementType.REPS,3,12),
            n("lsit_tuck","l_sit","Reverse Crunch","تَك الثبات في وضعية L (L-sit)","Tuck L-sit",3,MeasurementType.HOLD_SECONDS,3,20),
            n("lsit_full","l_sit","Reverse Crunch","الثبات في وضعية L (L-sit) كامل","L-sit",4,MeasurementType.HOLD_SECONDS,3,20)
        )
    )

    private fun muscleUp() = SkillGraph(
        "muscle_up", "الارتقاء فوق البار (Muscle-up)", "Muscle-up",
        listOf(
            n("mu_pull_base","muscle_up","Regular Pull Up","قاعدة السحب","Pull-up Base",1,MeasurementType.REPS,4,8,setOf("Pull-up Bar")),
            n("mu_dip_base","muscle_up","Chest Dip","قاعدة الدفع","Dip Base",2,MeasurementType.REPS,4,8,setOf("Parallel Bars")),
            n("mu_explosive","muscle_up","Regular Pull Up","السحب الانفجاري","Explosive Pull-up",3,MeasurementType.REPS,4,5,setOf("Pull-up Bar")),
            n("mu_transition","muscle_up","Regular Pull Up","مرحلة الانتقال","Transition",4,MeasurementType.QUALITY_REPS,3,5,setOf("Pull-up Bar")),
            n("mu_full","muscle_up","Regular Pull Up","الارتقاء فوق البار (Muscle-up)","Muscle-up",5,MeasurementType.REPS,3,3,setOf("Pull-up Bar"))
        )
    )

    private fun frontLever() = SkillGraph(
        "front_lever", "الثبات الأمامي المعلّق (Front Lever)", "Front Lever",
        listOf(
            n("fl_body_saw","front_lever","Body Saw","بلانك متحرك أمامًا وخلفًا","Body Saw",1,MeasurementType.REPS,3,12),
            n("fl_tuck","front_lever","Body Saw","الثبات الأمامي المطوي","Tuck Front Lever",2,MeasurementType.HOLD_SECONDS,3,15,setOf("Pull-up Bar")),
            n("fl_advanced","front_lever","Body Saw","الثبات الأمامي المطوي المتقدم","Advanced Tuck",3,MeasurementType.HOLD_SECONDS,3,12,setOf("Pull-up Bar")),
            n("fl_full","front_lever","Body Saw","الثبات الأمامي الكامل","Front Lever",4,MeasurementType.HOLD_SECONDS,3,10,setOf("Pull-up Bar"))
        )
    )

    private fun planche() = SkillGraph(
        "planche", "الثبات الأفقي على اليدين (Planche)", "Planche",
        listOf(
            n("pl_pseudo","planche","Pseudo Push Up","ضغط مائل للأمام","Pseudo Push-up",1,MeasurementType.REPS,3,10),
            n("pl_lean","planche","Pseudo Push Up","ميل الثبات الأفقي على اليدين (Planche)","Planche Lean",2,MeasurementType.HOLD_SECONDS,3,25),
            n("pl_tuck","planche","Pseudo Push Up","الثبات الأفقي المطوي","Tuck Planche",3,MeasurementType.HOLD_SECONDS,3,12),
            n("pl_full","planche","Pseudo Push Up","الثبات الأفقي الكامل","Full Planche",4,MeasurementType.HOLD_SECONDS,3,8)
        )
    )

    private fun pistolSquat() = SkillGraph(
        "pistol_squat", "القرفصاء بساق واحدة (Pistol Squat)", "Pistol Squat",
        listOf(
            n("ps_split","pistol_squat","Split Squat","قرفصاء بوضعية الاندفاع الثابت","Split Squat",1,MeasurementType.UNILATERAL_REPS,3,10),
            n("ps_bulgarian","pistol_squat","Bulgarian Split Squat","قرفصاء بلغارية","Bulgarian Split Squat",2,MeasurementType.UNILATERAL_REPS,3,10),
            n("ps_box","pistol_squat","Bulgarian Split Squat","قرفصاء بساق واحدة إلى مقعد","Box Pistol Squat",3,MeasurementType.UNILATERAL_REPS,3,8),
            n("ps_full","pistol_squat","Bulgarian Split Squat","قرفصاء بساق واحدة كاملة","Pistol Squat",4,MeasurementType.UNILATERAL_REPS,3,6)
        )
    )

    private fun n(
        id: String,
        skillId: String,
        exerciseName: String?,
        ar: String,
        en: String,
        level: Int,
        measurement: MeasurementType,
        sets: Int,
        target: Int,
        equipment: Set<String> = emptySet()
    ) = ProgressionNode(
        id = id,
        skillId = skillId,
        exerciseName = exerciseName,
        titleAr = ar,
        titleEn = en,
        level = level,
        measurementType = measurement,
        equipment = equipment,
        masteryRule = MasteryRule(sets, target)
    )
}

object SkillProgressionGraph {
    fun track(skillId: String): SkillGraph? = SkillGraphs.track(skillId)
    fun node(nodeId: String): ProgressionNode? = SkillGraphs.node(nodeId)
    fun next(nodeId: String): ProgressionNode? = SkillGraphs.next(nodeId)
    fun previous(nodeId: String): ProgressionNode? = SkillGraphs.previous(nodeId)
}

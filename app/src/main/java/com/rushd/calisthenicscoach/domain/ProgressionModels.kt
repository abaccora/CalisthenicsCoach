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
    val progressionNodeId: String? = null,
    val notesAr: String = ""
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
            node("pull_dead_hang","pull_up","Passive Hang","التعلق الحر","Dead Hang",1,MeasurementType.HOLD_SECONDS,30,3,setOf("Pull-up Bar"),next="pull_scapular"),
            node("pull_scapular","pull_up","Scapular Pull Up","سحب لوح الكتف","Scapular Pull-up",2,MeasurementType.REPS,10,3,setOf("Pull-up Bar"),prev="pull_dead_hang",next="pull_assisted"),
            node("pull_assisted","pull_up","Parallel Grip Pull Up","عقلة بمساعدة","Assisted Pull-up",3,MeasurementType.REPS,8,3,setOf("Pull-up Bar"),prev="pull_scapular",next="pull_regular"),
            node("pull_regular","pull_up","Regular Pull Up","العقلة","Pull-up",4,MeasurementType.REPS,8,3,setOf("Pull-up Bar"),prev="pull_assisted",next="pull_explosive"),
            node("pull_explosive","pull_up","Regular Pull Up","العقلة الانفجارية","Explosive Pull-up",5,MeasurementType.REPS,5,4,setOf("Pull-up Bar"),prev="pull_regular")
        )
    )

    val dip = SkillGraph(
        skillId = "dip",
        titleAr = "المتوازي",
        titleEn = "Dip",
        nodes = listOf(
            node("dip_bench","dip","Bench Dip","ديبس على مقعد","Bench Dip",1,MeasurementType.REPS,12,3,next="dip_support"),
            node("dip_support","dip","Parallel Bar Support Hold","الثبات على المتوازي","Support Hold",2,MeasurementType.HOLD_SECONDS,30,3,setOf("Parallel Bars"),prev="dip_bench",next="dip_regular"),
            node("dip_regular","dip","Chest Dip","المتوازي","Dip",3,MeasurementType.REPS,10,3,setOf("Parallel Bars"),prev="dip_support",next="dip_deep"),
            node("dip_deep","dip","Chest Dip","المتوازي العميق","Deep Dip",4,MeasurementType.REPS,8,3,setOf("Parallel Bars"),prev="dip_regular")
        )
    )

    val handstand = SkillGraph(
        skillId = "handstand",
        titleAr = "الوقوف على اليدين",
        titleEn = "Handstand",
        nodes = listOf(
            node("hs_pike","handstand","Pike Push Up","ضغط بايك","Pike Push-up",1,MeasurementType.REPS,10,3,next="hs_shoulder_tap"),
            node("hs_shoulder_tap","handstand","Plank Shoulder Tap","لمس الكتف من البلانك","Shoulder Tap",2,MeasurementType.UNILATERAL_REPS,10,3,prev="hs_pike",next="hs_wall"),
            node("hs_wall","handstand","Wall Handstand","وقوف على اليدين بمساعدة الحائط","Wall Handstand",3,MeasurementType.HOLD_SECONDS,30,3,prev="hs_shoulder_tap",next="hs_toe_pull"),
            node("hs_toe_pull","handstand","Wall Toe Pull","انفصال قصير عن الحائط","Wall Toe Pull",4,MeasurementType.HOLD_SECONDS,20,3,prev="hs_wall",next="hs_free"),
            node("hs_free","handstand","Freestanding Handstand","وقوف حر على اليدين","Freestanding Handstand",5,MeasurementType.HOLD_SECONDS,20,3,prev="hs_toe_pull")
        )
    )

    val lSit = SkillGraph(
        skillId = "l_sit",
        titleAr = "إل-سِت",
        titleEn = "L-sit",
        nodes = listOf(
            node("lsit_hollow","l_sit","Hollow Body Hold","الثبات المجوف","Hollow Hold",1,MeasurementType.HOLD_SECONDS,30,3,next="lsit_compression"),
            node("lsit_compression","l_sit","Seated Compression Lift","رفع القدمين من الجلوس","Seated Compression Lift",2,MeasurementType.REPS,10,3,prev="lsit_hollow",next="lsit_tuck"),
            node("lsit_tuck","l_sit","Tuck L-sit","تَك إل-سِت","Tuck L-sit",3,MeasurementType.HOLD_SECONDS,20,3,prev="lsit_compression",next="lsit_one_leg"),
            node("lsit_one_leg","l_sit","One-leg L-sit","إل-سِت بساق واحدة","One-leg L-sit",4,MeasurementType.HOLD_SECONDS,15,3,prev="lsit_tuck",next="lsit_full"),
            node("lsit_full","l_sit","L-sit","إل-سِت كامل","L-sit",5,MeasurementType.HOLD_SECONDS,20,3,prev="lsit_one_leg")
        )
    )

    val muscleUp = SkillGraph(
        skillId = "muscle_up",
        titleAr = "المَسِل أب",
        titleEn = "Muscle-up",
        nodes = listOf(
            node("mu_pull_base","muscle_up","Regular Pull Up","قاعدة السحب","Pull-up Base",1,MeasurementType.REPS,8,4,setOf("Pull-up Bar"),next="mu_dip_base"),
            node("mu_dip_base","muscle_up","Chest Dip","قاعدة الدفع","Dip Base",2,MeasurementType.REPS,10,3,setOf("Parallel Bars"),prev="mu_pull_base",next="mu_explosive"),
            node("mu_explosive","muscle_up","Explosive Chest Pull-up","سحب انفجاري إلى الصدر","Explosive Chest Pull-up",3,MeasurementType.REPS,5,3,setOf("Pull-up Bar"),prev="mu_dip_base",next="mu_transition"),
            node("mu_transition","muscle_up","Low Muscle-up Transition","انتقال المَسِل أب","Low Transition",4,MeasurementType.REPS,5,3,setOf("Pull-up Bar"),prev="mu_explosive",next="mu_full"),
            node("mu_full","muscle_up","Muscle-up","مَسِل أب","Muscle-up",5,MeasurementType.REPS,3,3,setOf("Pull-up Bar"),prev="mu_transition")
        )
    )

    val frontLever = SkillGraph(
        skillId = "front_lever",
        titleAr = "الفرونت ليفر",
        titleEn = "Front Lever",
        nodes = listOf(
            node("fl_body_saw","front_lever","Body Saw","بودي سو","Body Saw",1,MeasurementType.REPS,12,3,next="fl_tuck"),
            node("fl_tuck","front_lever","Tuck Front Lever","تَك فرونت ليفر","Tuck Front Lever",2,MeasurementType.HOLD_SECONDS,15,3,setOf("Pull-up Bar"),prev="fl_body_saw",next="fl_adv_tuck"),
            node("fl_adv_tuck","front_lever","Advanced Tuck Front Lever","تَك متقدم","Advanced Tuck",3,MeasurementType.HOLD_SECONDS,12,3,setOf("Pull-up Bar"),prev="fl_tuck",next="fl_one_leg"),
            node("fl_one_leg","front_lever","One-leg Front Lever","فرونت ليفر بساق واحدة","One-leg Front Lever",4,MeasurementType.HOLD_SECONDS,10,3,setOf("Pull-up Bar"),prev="fl_adv_tuck",next="fl_full"),
            node("fl_full","front_lever","Front Lever","فرونت ليفر كامل","Front Lever",5,MeasurementType.HOLD_SECONDS,10,3,setOf("Pull-up Bar"),prev="fl_one_leg")
        )
    )

    val planche = SkillGraph(
        skillId = "planche",
        titleAr = "البلانش",
        titleEn = "Planche",
        nodes = listOf(
            node("pl_pseudo","planche","Pseudo Push Up","ضغط سودو","Pseudo Push-up",1,MeasurementType.REPS,10,3,next="pl_lean"),
            node("pl_lean","planche","Planche Lean","ميل البلانش","Planche Lean",2,MeasurementType.HOLD_SECONDS,25,3,prev="pl_pseudo",next="pl_tuck"),
            node("pl_tuck","planche","Tuck Planche","تَك بلانش","Tuck Planche",3,MeasurementType.HOLD_SECONDS,12,3,prev="pl_lean",next="pl_adv_tuck"),
            node("pl_adv_tuck","planche","Advanced Tuck Planche","تَك بلانش متقدم","Advanced Tuck Planche",4,MeasurementType.HOLD_SECONDS,10,3,prev="pl_tuck",next="pl_full"),
            node("pl_full","planche","Full Planche","بلانش كامل","Full Planche",5,MeasurementType.HOLD_SECONDS,8,3,prev="pl_adv_tuck")
        )
    )

    val pistolSquat = SkillGraph(
        skillId = "pistol_squat",
        titleAr = "القرفصاء بساق واحدة",
        titleEn = "Pistol Squat",
        nodes = listOf(
            node("ps_split","pistol_squat","Split Squat","قرفصاء منفصلة","Split Squat",1,MeasurementType.UNILATERAL_REPS,10,3,next="ps_bulgarian"),
            node("ps_bulgarian","pistol_squat","Bulgarian Split Squat","قرفصاء بلغارية","Bulgarian Split Squat",2,MeasurementType.UNILATERAL_REPS,10,3,prev="ps_split",next="ps_box"),
            node("ps_box","pistol_squat","Box Pistol Squat","قرفصاء بساق واحدة إلى مقعد","Box Pistol Squat",3,MeasurementType.UNILATERAL_REPS,8,3,prev="ps_bulgarian",next="ps_assisted"),
            node("ps_assisted","pistol_squat","Assisted Pistol Squat","قرفصاء بساق واحدة بمساعدة","Assisted Pistol Squat",4,MeasurementType.UNILATERAL_REPS,8,3,prev="ps_box",next="ps_full"),
            node("ps_full","pistol_squat","Pistol Squat","قرفصاء كاملة بساق واحدة","Pistol Squat",5,MeasurementType.UNILATERAL_REPS,6,3,prev="ps_assisted")
        )
    )

    val all = listOf(pullUp, dip, handstand, lSit, muscleUp, frontLever, planche, pistolSquat)

    private val nodeIndex = all.flatMap { it.nodes }.associateBy { it.id }

    fun byId(skillId: String): SkillGraph? = all.firstOrNull { it.skillId == skillId }
    fun node(nodeId: String): ProgressionNode? = nodeIndex[nodeId]
    fun next(nodeId: String): ProgressionNode? = node(nodeId)?.progressionNodeId?.let(nodeIndex::get)
    fun previous(nodeId: String): ProgressionNode? = node(nodeId)?.regressionNodeId?.let(nodeIndex::get)

    private fun node(
        id: String,
        skillId: String,
        exerciseName: String,
        titleAr: String,
        titleEn: String,
        level: Int,
        type: MeasurementType,
        target: Int,
        sets: Int,
        equipment: Set<String> = emptySet(),
        prev: String? = null,
        next: String? = null
    ) = ProgressionNode(
        id = id,
        skillId = skillId,
        exerciseName = exerciseName,
        titleAr = titleAr,
        titleEn = titleEn,
        level = level,
        measurementType = type,
        equipment = equipment,
        masteryRule = MasteryRule(
            minSets = sets,
            targetValue = target
        ),
        regressionNodeId = prev,
        progressionNodeId = next
    )
}

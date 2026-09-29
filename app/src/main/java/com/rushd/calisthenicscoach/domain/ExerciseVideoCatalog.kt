package com.rushd.calisthenicscoach.domain

import com.rushd.calisthenicscoach.R

data class ExerciseVideo(val key: String, val nameEn: String, val nameAr: String, val category: String, val resId: Int)

object ExerciseVideoCatalog {
    val all = listOf(
        ExerciseVideo("air_squat", "Air Squat", "قرفصاء بوزن الجسم", "Legs", R.raw.air_squat),
        ExerciseVideo("alternating_superman", "Alternating Superman", "سوبرمان متبادل", "Back", R.raw.alternating_superman),
        ExerciseVideo("bench_dip", "Bench Dip", "ديبس على مقعد", "Arms & Shoulders", R.raw.bench_dip),
        ExerciseVideo("bench_kneeling_lat_stretch", "Bench Kneeling Lat Stretch", "إطالة الظهر على المقعد", "Mobility", R.raw.bench_kneeling_lat_stretch),
        ExerciseVideo("bicycle_crunch", "Bicycle Crunch", "كرنش الدراجة", "Core", R.raw.bicycle_crunch),
        ExerciseVideo("body_saw", "Body Saw", "بودي سو", "Arms & Shoulders", R.raw.body_saw),
        ExerciseVideo("bulgarian_split_squat", "Bulgarian Split Squat", "قرفصاء بلغارية", "Legs", R.raw.bulgarian_split_squat),
        ExerciseVideo("burpee", "Burpee", "بيربي", "Cardio", R.raw.burpee),
        ExerciseVideo("butt_kicks", "Butt Kicks", "ركلات خلفية", "Cardio", R.raw.butt_kicks),
        ExerciseVideo("calf_raise", "Calf Raise", "رفع السمانة", "Legs", R.raw.calf_raise),
        ExerciseVideo("cat_cow_stretch", "Cat Cow Stretch", "إطالة القطة والبقرة", "Mobility", R.raw.cat_cow_stretch),
        ExerciseVideo("chest_dip", "Chest Dip", "ديبس للصدر", "Chest", R.raw.chest_dip),
        ExerciseVideo("child_s_pose_back_stretch", "Child's Pose Back Stretch", "وضعية الطفل لإطالة الظهر", "Mobility", R.raw.child_s_pose_back_stretch),
        ExerciseVideo("crunch", "Crunch", "كرنش", "Core", R.raw.crunch),
        ExerciseVideo("dead_bug", "Dead Bug", "ديد باغ", "Core", R.raw.dead_bug),
        ExerciseVideo("decline_push_up", "Decline Push Up", "ضغط مائل للأسفل", "Chest", R.raw.decline_push_up),
        ExerciseVideo("deficit_push_up", "Deficit Push Up", "ضغط بمدى عميق", "Chest", R.raw.deficit_push_up),
        ExerciseVideo("doorway_chest_stretch", "Doorway Chest Stretch", "إطالة الصدر عند الباب", "Mobility", R.raw.doorway_chest_stretch),
        ExerciseVideo("downward_facing_dog", "Downward Facing Dog", "وضعية الكلب لأسفل", "Mobility", R.raw.downward_facing_dog),
        ExerciseVideo("high_knee_taps", "High Knee Taps", "رفع الركبتين", "Cardio", R.raw.high_knee_taps),
        ExerciseVideo("hip_flexor_stretch", "Hip Flexor Stretch", "إطالة مثنيات الورك", "Mobility", R.raw.hip_flexor_stretch),
        ExerciseVideo("hip_thrust", "Hip Thrust", "دفع الورك", "Legs", R.raw.hip_thrust),
        ExerciseVideo("incline_push_up", "Incline Push Up", "ضغط مائل", "Chest", R.raw.incline_push_up),
        ExerciseVideo("inverted_row", "Inverted Row", "سحب أفقي", "Back", R.raw.inverted_row),
        ExerciseVideo("jump_rope", "Jump Rope", "نط الحبل", "Cardio", R.raw.jump_rope),
        ExerciseVideo("jumping_jack", "Jumping Jack", "جامبنغ جاك", "Cardio", R.raw.jumping_jack),
        ExerciseVideo("kneeling_hamstring_stretch", "Kneeling Hamstring Stretch", "إطالة أوتار الركبة", "Mobility", R.raw.kneeling_hamstring_stretch),
        ExerciseVideo("lunging_calf_stretch", "Lunging Calf Stretch", "إطالة السمانة", "Mobility", R.raw.lunging_calf_stretch),
        ExerciseVideo("lying_back_extension", "Lying Back Extension", "تمديد الظهر أرضًا", "Back", R.raw.lying_back_extension),
        ExerciseVideo("lying_glute_stretch", "Lying Glute Stretch", "إطالة الألوية", "Mobility", R.raw.lying_glute_stretch),
        ExerciseVideo("overhead_tricep_stretch", "Overhead Tricep Stretch", "إطالة الترايسبس فوق الرأس", "Mobility", R.raw.overhead_tricep_stretch),
        ExerciseVideo("parallel_grip_pull_up", "Parallel Grip Pull Up", "عقلة بقبضة متوازية", "Back", R.raw.parallel_grip_pull_up),
        ExerciseVideo("pike_push_up", "Pike Push Up", "ضغط بايك", "Arms & Shoulders", R.raw.pike_push_up),
        ExerciseVideo("plank_shoulder_tap", "Plank Shoulder Tap", "لمس الكتف من البلانك", "Arms & Shoulders", R.raw.plank_shoulder_tap),
        ExerciseVideo("pseudo_push_up", "Pseudo Push Up", "ضغط سودو", "Arms & Shoulders", R.raw.pseudo_push_up),
        ExerciseVideo("rear_deltoid_stretch", "Rear Deltoid Stretch", "إطالة الكتف الخلفي", "Mobility", R.raw.rear_deltoid_stretch),
        ExerciseVideo("regular_pull_up", "Regular Pull Up", "عقلة", "Back", R.raw.regular_pull_up),
        ExerciseVideo("regular_push_up", "Regular Push Up", "ضغط", "Chest", R.raw.regular_push_up),
        ExerciseVideo("reverse_crunch", "Reverse Crunch", "كرنش عكسي", "Core", R.raw.reverse_crunch),
        ExerciseVideo("reverse_hyperextension", "Reverse Hyperextension", "هايبر إكستنشن عكسي", "Legs", R.raw.reverse_hyperextension),
        ExerciseVideo("running_in_place_punches", "Running in Place Punches", "جري في المكان مع لكمات", "Cardio", R.raw.running_in_place_punches),
        ExerciseVideo("running_in_place", "Running in Place", "جري في المكان", "Cardio", R.raw.running_in_place),
        ExerciseVideo("single_leg_deadlift", "SIngle Leg Deadlift", "ديدلفت بساق واحدة", "Legs", R.raw.single_leg_deadlift),
        ExerciseVideo("side_crunch", "Side Crunch", "كرنش جانبي", "Core", R.raw.side_crunch),
        ExerciseVideo("side_lunge_stretch", "Side Lunge Stretch", "إطالة اندفاع جانبي", "Mobility", R.raw.side_lunge_stretch),
        ExerciseVideo("side_tilt", "Side Tilt", "ميل جانبي", "Mobility", R.raw.side_tilt),
        ExerciseVideo("sit_up", "Sit Up", "سيت أب", "Core", R.raw.sit_up),
        ExerciseVideo("split_squat", "Split Squat", "قرفصاء منفصلة", "Legs", R.raw.split_squat),
        ExerciseVideo("standing_forward_bending", "Standing Forward Bending", "انحناء أمامي واقف", "Mobility", R.raw.standing_forward_bending),
        ExerciseVideo("standing_knee_to_chest_stretch", "Standing Knee to Chest Stretch", "إطالة الركبة إلى الصدر", "Mobility", R.raw.standing_knee_to_chest_stretch),
        ExerciseVideo("standing_quadricep_stretch", "Standing Quadricep Stretch", "إطالة العضلة الأمامية للفخذ", "Mobility", R.raw.standing_quadricep_stretch),
        ExerciseVideo("sumo_squat", "Sumo Squat", "قرفصاء سومو", "Legs", R.raw.sumo_squat),
        ExerciseVideo("tricep_extension", "Tricep Extension", "تمديد الترايسبس", "Arms & Shoulders", R.raw.tricep_extension),
        ExerciseVideo("upward_facing_dog", "Upward Facing Dog", "وضعية الكلب لأعلى", "Mobility", R.raw.upward_facing_dog),
    )

    private val aliases = mapOf(
        "incline push-up" to "incline_push_up",
        "body row" to "inverted_row",
        "split squat" to "split_squat",
        "dead bug" to "dead_bug",
        "pull-up" to "regular_pull_up",
        "parallel bar dip" to "chest_dip",
    )

    fun forExercise(name: String): ExerciseVideo? {
        val normalized = name.lowercase().replace("–", "-").trim()
        aliases[normalized]?.let { key -> return all.firstOrNull { it.key == key } }
        val loose = normalized.replace("-", " ").replace(Regex("\\s+"), " ")
        return all.firstOrNull { it.nameEn.lowercase().replace("-", " ") == loose }
    }
}

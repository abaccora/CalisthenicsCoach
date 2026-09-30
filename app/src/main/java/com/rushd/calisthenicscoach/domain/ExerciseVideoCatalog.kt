package com.rushd.calisthenicscoach.domain

import com.rushd.calisthenicscoach.R

data class ExerciseVideo(
    val key: String,
    val nameEn: String,
    val nameAr: String,
    val category: String,
    val resId: Int? = null,
    val assetPath: String? = null,
    val source: String = "LEGACY"
)

object ExerciseVideoCatalog {
    private val legacy = listOf(
        ExerciseVideo("air_squat", "Air Squat", ArabicTerminology.exercise("Air Squat"), "Legs", R.raw.air_squat),
        ExerciseVideo("alternating_superman", "Alternating Superman", ArabicTerminology.exercise("Alternating Superman"), "Back", R.raw.alternating_superman),
        ExerciseVideo("bench_dip", "Bench Dip", ArabicTerminology.exercise("Bench Dip"), "Arms & Shoulders", R.raw.bench_dip),
        ExerciseVideo("bench_kneeling_lat_stretch", "Bench Kneeling Lat Stretch", ArabicTerminology.exercise("Bench Kneeling Lat Stretch"), "Mobility", R.raw.bench_kneeling_lat_stretch),
        ExerciseVideo("bicycle_crunch", "Bicycle Crunch", ArabicTerminology.exercise("Bicycle Crunch"), "Core", R.raw.bicycle_crunch),
        ExerciseVideo("body_saw", "Body Saw", ArabicTerminology.exercise("Body Saw"), "Arms & Shoulders", R.raw.body_saw),
        ExerciseVideo("bulgarian_split_squat", "Bulgarian Split Squat", ArabicTerminology.exercise("Bulgarian Split Squat"), "Legs", R.raw.bulgarian_split_squat),
        ExerciseVideo("burpee", "Burpee", ArabicTerminology.exercise("Burpee"), "Cardio", R.raw.burpee),
        ExerciseVideo("butt_kicks", "Butt Kicks", ArabicTerminology.exercise("Butt Kicks"), "Cardio", R.raw.butt_kicks),
        ExerciseVideo("calf_raise", "Calf Raise", ArabicTerminology.exercise("Calf Raise"), "Legs", R.raw.calf_raise),
        ExerciseVideo("cat_cow_stretch", "Cat Cow Stretch", ArabicTerminology.exercise("Cat Cow Stretch"), "Mobility", R.raw.cat_cow_stretch),
        ExerciseVideo("chest_dip", "Chest Dip", ArabicTerminology.exercise("Chest Dip"), "Chest", R.raw.chest_dip),
        ExerciseVideo("child_s_pose_back_stretch", "Child's Pose Back Stretch", ArabicTerminology.exercise("Child's Pose Back Stretch"), "Mobility", R.raw.child_s_pose_back_stretch),
        ExerciseVideo("crunch", "Crunch", ArabicTerminology.exercise("Crunch"), "Core", R.raw.crunch),
        ExerciseVideo("dead_bug", "Dead Bug", ArabicTerminology.exercise("Dead Bug"), "Core", R.raw.dead_bug),
        ExerciseVideo("decline_push_up", "Decline Push Up", ArabicTerminology.exercise("Decline Push Up"), "Chest", R.raw.decline_push_up),
        ExerciseVideo("deficit_push_up", "Deficit Push Up", ArabicTerminology.exercise("Deficit Push Up"), "Chest", R.raw.deficit_push_up),
        ExerciseVideo("doorway_chest_stretch", "Doorway Chest Stretch", ArabicTerminology.exercise("Doorway Chest Stretch"), "Mobility", R.raw.doorway_chest_stretch),
        ExerciseVideo("downward_facing_dog", "Downward Facing Dog", ArabicTerminology.exercise("Downward Facing Dog"), "Mobility", R.raw.downward_facing_dog),
        ExerciseVideo("high_knee_taps", "High Knee Taps", ArabicTerminology.exercise("High Knee Taps"), "Cardio", R.raw.high_knee_taps),
        ExerciseVideo("hip_flexor_stretch", "Hip Flexor Stretch", ArabicTerminology.exercise("Hip Flexor Stretch"), "Mobility", R.raw.hip_flexor_stretch),
        ExerciseVideo("hip_thrust", "Hip Thrust", ArabicTerminology.exercise("Hip Thrust"), "Legs", R.raw.hip_thrust),
        ExerciseVideo("incline_push_up", "Incline Push Up", ArabicTerminology.exercise("Incline Push Up"), "Chest", R.raw.incline_push_up),
        ExerciseVideo("inverted_row", "Inverted Row", ArabicTerminology.exercise("Inverted Row"), "Back", R.raw.inverted_row),
        ExerciseVideo("jump_rope", "Jump Rope", ArabicTerminology.exercise("Jump Rope"), "Cardio", R.raw.jump_rope),
        ExerciseVideo("jumping_jack", "Jumping Jack", ArabicTerminology.exercise("Jumping Jack"), "Cardio", R.raw.jumping_jack),
        ExerciseVideo("kneeling_hamstring_stretch", "Kneeling Hamstring Stretch", ArabicTerminology.exercise("Kneeling Hamstring Stretch"), "Mobility", R.raw.kneeling_hamstring_stretch),
        ExerciseVideo("lunging_calf_stretch", "Lunging Calf Stretch", ArabicTerminology.exercise("Lunging Calf Stretch"), "Mobility", R.raw.lunging_calf_stretch),
        ExerciseVideo("lying_back_extension", "Lying Back Extension", ArabicTerminology.exercise("Lying Back Extension"), "Back", R.raw.lying_back_extension),
        ExerciseVideo("lying_glute_stretch", "Lying Glute Stretch", ArabicTerminology.exercise("Lying Glute Stretch"), "Mobility", R.raw.lying_glute_stretch),
        ExerciseVideo("overhead_tricep_stretch", "Overhead Tricep Stretch", ArabicTerminology.exercise("Overhead Tricep Stretch"), "Mobility", R.raw.overhead_tricep_stretch),
        ExerciseVideo("parallel_grip_pull_up", "Parallel Grip Pull Up", ArabicTerminology.exercise("Parallel Grip Pull Up"), "Back", R.raw.parallel_grip_pull_up),
        ExerciseVideo("pike_push_up", "Pike Push Up", ArabicTerminology.exercise("Pike Push Up"), "Arms & Shoulders", R.raw.pike_push_up),
        ExerciseVideo("plank_shoulder_tap", "Plank Shoulder Tap", ArabicTerminology.exercise("Plank Shoulder Tap"), "Arms & Shoulders", R.raw.plank_shoulder_tap),
        ExerciseVideo("pseudo_push_up", "Pseudo Push Up", ArabicTerminology.exercise("Pseudo Push Up"), "Arms & Shoulders", R.raw.pseudo_push_up),
        ExerciseVideo("rear_deltoid_stretch", "Rear Deltoid Stretch", ArabicTerminology.exercise("Rear Deltoid Stretch"), "Mobility", R.raw.rear_deltoid_stretch),
        ExerciseVideo("regular_pull_up", "Regular Pull Up", ArabicTerminology.exercise("Regular Pull Up"), "Back", R.raw.regular_pull_up),
        ExerciseVideo("regular_push_up", "Regular Push Up", ArabicTerminology.exercise("Regular Push Up"), "Chest", R.raw.regular_push_up),
        ExerciseVideo("reverse_crunch", "Reverse Crunch", ArabicTerminology.exercise("Reverse Crunch"), "Core", R.raw.reverse_crunch),
        ExerciseVideo("reverse_hyperextension", "Reverse Hyperextension", ArabicTerminology.exercise("Reverse Hyperextension"), "Legs", R.raw.reverse_hyperextension),
        ExerciseVideo("running_in_place_punches", "Running in Place Punches", ArabicTerminology.exercise("Running in Place Punches"), "Cardio", R.raw.running_in_place_punches),
        ExerciseVideo("running_in_place", "Running in Place", ArabicTerminology.exercise("Running in Place"), "Cardio", R.raw.running_in_place),
        ExerciseVideo("single_leg_deadlift", "SIngle Leg Deadlift", ArabicTerminology.exercise("SIngle Leg Deadlift"), "Legs", R.raw.single_leg_deadlift),
        ExerciseVideo("side_crunch", "Side Crunch", ArabicTerminology.exercise("Side Crunch"), "Core", R.raw.side_crunch),
        ExerciseVideo("side_lunge_stretch", "Side Lunge Stretch", ArabicTerminology.exercise("Side Lunge Stretch"), "Mobility", R.raw.side_lunge_stretch),
        ExerciseVideo("side_tilt", "Side Tilt", ArabicTerminology.exercise("Side Tilt"), "Mobility", R.raw.side_tilt),
        ExerciseVideo("sit_up", "Sit Up", ArabicTerminology.exercise("Sit Up"), "Core", R.raw.sit_up),
        ExerciseVideo("split_squat", "Split Squat", ArabicTerminology.exercise("Split Squat"), "Legs", R.raw.split_squat),
        ExerciseVideo("standing_forward_bending", "Standing Forward Bending", ArabicTerminology.exercise("Standing Forward Bending"), "Mobility", R.raw.standing_forward_bending),
        ExerciseVideo("standing_knee_to_chest_stretch", "Standing Knee to Chest Stretch", ArabicTerminology.exercise("Standing Knee to Chest Stretch"), "Mobility", R.raw.standing_knee_to_chest_stretch),
        ExerciseVideo("standing_quadricep_stretch", "Standing Quadricep Stretch", ArabicTerminology.exercise("Standing Quadricep Stretch"), "Mobility", R.raw.standing_quadricep_stretch),
        ExerciseVideo("sumo_squat", "Sumo Squat", ArabicTerminology.exercise("Sumo Squat"), "Legs", R.raw.sumo_squat),
        ExerciseVideo("tricep_extension", "Tricep Extension", ArabicTerminology.exercise("Tricep Extension"), "Arms & Shoulders", R.raw.tricep_extension),
        ExerciseVideo("upward_facing_dog", "Upward Facing Dog", ArabicTerminology.exercise("Upward Facing Dog"), "Mobility", R.raw.upward_facing_dog),
    )

    val all: List<ExerciseVideo> = HomeWorkoutMediaCatalog.all

    val legacyFallback: List<ExerciseVideo> = legacy

    private val aliases = mapOf(
        "incline push-up" to "incline_push_up",
        "body row" to "inverted_row",
        "split squat" to "split_squat",
        "dead bug" to "dead_bug",
        "pull-up" to "regular_pull_up",
        "passive hang" to "regular_pull_up",
        "parallel bar dip" to "chest_dip",
    )

    fun forExercise(name: String): ExerciseVideo? {
        HomeWorkoutMediaCatalog.forExercise(name)?.let { return it }

        val normalized = name.lowercase().replace("–", "-").trim()
        aliases[normalized]?.let { key -> return legacy.firstOrNull { it.key == key } }
        val loose = normalized.replace("-", " ").replace(Regex("\\s+"), " ")
        return legacy.firstOrNull { it.nameEn.lowercase().replace("-", " ") == loose }
    }

    fun allWithFallback(): List<ExerciseVideo> = all + legacy
}

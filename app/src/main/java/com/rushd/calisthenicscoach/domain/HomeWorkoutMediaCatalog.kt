package com.rushd.calisthenicscoach.domain

/**
 * Modern local video pack extracted from the user-provided Home Workout library.
 *
 * Files are injected under assets/homework/ in the installable test build.
 * Labels here are descriptive labels used by Calisthenics Coach; they are not
 * intended to reproduce source-app wording.
 */
object HomeWorkoutMediaCatalog {

    private fun v(
        id: Int,
        nameEn: String,
        nameAr: String,
        category: String
    ) = ExerciseVideo(
        key = "hw_$id",
        nameEn = nameEn,
        nameAr = nameAr,
        category = category,
        assetPath = "homework/action_$id.mp4",
        source = "HOME_WORKOUT"
    )

    val all: List<ExerciseVideo> = listOf(
        v(0, "Reach Crunch", "كرنش مع مد الذراعين", "Core"),
        v(17, "Wall Calf Raise", "رفع الكعبين مع دعم الحائط", "Legs"),
        v(21, "Standing Arm Reach", "مد الذراعين من الوقوف", "Mobility"),
        v(22, "Side Lunge", "اندفاع جانبي", "Legs"),
        v(48, "Glute Bridge", "جسر الألوية", "Legs"),
        v(61, "Arm Circle Warm-up", "دوائر الذراعين", "Mobility"),
        v(62, "Standing Punches", "لكمات من الوقوف", "Cardio"),
        v(65, "Wrist and Forearm Mobility", "تحريك الرسغ والساعد", "Mobility"),
        v(120, "Shoulder Warm-up", "تهيئة الكتفين", "Mobility"),
        v(127, "Heel Touches", "لمس الكعبين للبطن", "Core"),
        v(129, "Side Lunge Stretch", "إطالة الاندفاع الجانبي", "Mobility"),
        v(130, "Arm Swings", "مرجحة الذراعين", "Mobility"),
        v(149, "Standing Forward Bend", "انحناء أمامي من الوقوف", "Mobility"),
        v(155, "Shoulder Rotation", "دوران الكتف", "Mobility"),
        v(163, "Wrist Circles", "دوائر الرسغ", "Mobility"),
        v(167, "High Knees", "رفع الركبتين عاليًا", "Cardio"),
        v(177, "Dead Bug", "تمرين الجذع المتعاكس", "Core"),
        v(187, "Hip Hinge", "انحناء الورك", "Legs"),
        v(238, "Overhead Triceps Stretch", "إطالة العضلة ثلاثية الرؤوس فوق الرأس", "Mobility"),
        v(263, "Upper Back and Neck Mobility", "تحريك أعلى الظهر والرقبة", "Mobility"),
        v(298, "Overhead Side Bend", "ميل جانبي فوق الرأس", "Mobility"),
        v(299, "Overhead Side Stretch", "إطالة جانبية فوق الرأس", "Mobility"),
        v(328, "Squat Warm-up", "تهيئة القرفصاء", "Legs"),
        v(329, "March in Place", "مشي في المكان", "Cardio"),
        v(355, "Prone Leg Raise", "رفع الساقين من الانبطاح", "Back"),
        v(387, "Standing Calf Stretch Left", "إطالة الساق من الوقوف - يسار", "Mobility"),
        v(388, "Standing Calf Stretch Right", "إطالة الساق من الوقوف - يمين", "Mobility"),
        v(477, "Standing Hip Abduction Left", "رفع الساق جانبًا مع دعم - يسار", "Legs"),
        v(478, "Standing Hip Abduction Right", "رفع الساق جانبًا مع دعم - يمين", "Legs"),
        v(2836, "Cross-body Shoulder Warm-up", "تهيئة الكتف عبر الجسم", "Mobility"),
        v(2839, "Shoulder Rotation Warm-up", "دوران الكتفين للإحماء", "Mobility"),
        v(2840, "Large Arm Circles", "دوائر ذراعين واسعة", "Mobility"),
        v(2847, "Reverse Lunge Left", "اندفاع خلفي - يسار", "Legs"),
        v(2848, "Reverse Lunge Right", "اندفاع خلفي - يمين", "Legs"),
        v(2849, "Bodyweight Squat", "قرفصاء بوزن الجسم", "Legs"),
        v(2850, "Standing Posture Reset", "تهيئة الوقوف والوضعية", "Mobility"),
        v(2860, "Standing Hip Mobility Left", "تحريك الورك من الوقوف - يسار", "Mobility"),
        v(2861, "Standing Hip Mobility Right", "تحريك الورك من الوقوف - يمين", "Mobility"),
        v(2872, "Hip Flexor Stretch Left", "إطالة مثنيات الورك - يسار", "Mobility"),
        v(2873, "Hip Flexor Stretch Right", "إطالة مثنيات الورك - يمين", "Mobility"),
        v(2895, "Bird Dog", "رفع الذراع والساق المتعاكسين", "Core"),
        v(2896, "Cat Cow", "إطالة القطة والبقرة", "Mobility"),
        v(2914, "Kneeling Push-up", "تمرين الضغط من الركبتين", "Chest"),
        v(2925, "Plank to Downward Dog", "الانتقال من البلانك إلى وضعية الكلب لأسفل", "Mobility"),
        v(2932, "Prone Hamstring Curl", "ثني الركبة من الانبطاح", "Legs"),
        v(2941, "Superman", "رفع الجذع والأطراف من الانبطاح", "Back"),
        v(2947, "Child's Pose", "وضعية الطفل", "Mobility"),
        v(2957, "Scissor Kicks", "ركلات المقص للبطن", "Core"),
        v(2968, "Core Curl-up", "انقباض الجذع والبطن", "Core")
    )

    private val exerciseAliases: Map<String, String> = mapOf(
        "Air Squat" to "hw_2849",
        "Alternating Superman" to "hw_2895",
        "Calf Raise" to "hw_17",
        "Cat Cow Stretch" to "hw_2896",
        "Child's Pose Back Stretch" to "hw_2947",
        "Crunch" to "hw_0",
        "Dead Bug" to "hw_177",
        "Downward Facing Dog" to "hw_2925",
        "High Knee Taps" to "hw_167",
        "Hip Flexor Stretch" to "hw_2872",
        "Hip Thrust" to "hw_48",
        "Overhead Tricep Stretch" to "hw_238",
        "Running in Place Punches" to "hw_62",
        "Side Lunge Stretch" to "hw_129",
        "Side Tilt" to "hw_298",
        "Standing Forward Bending" to "hw_149"
    ).mapKeys { it.key.lowercase() }

    fun forExercise(name: String): ExerciseVideo? {
        val key = exerciseAliases[name.trim().lowercase()] ?: return null
        return all.firstOrNull { it.key == key }
    }
}

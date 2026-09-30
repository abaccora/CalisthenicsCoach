package com.rushd.calisthenicscoach.domain

/**
 * Canonical Arabic terminology used across the app.
 *
 * The UI prefers clear descriptive Arabic. When a calisthenics skill has no
 * widely established Arabic name, the English term is retained in parentheses
 * after a descriptive Arabic label.
 */
object ArabicTerminology {

    fun goal(value: String): String = when (value) {
        "Pull-up" -> "السحب على البار (Pull-up)"
        "Muscle-up" -> "الارتقاء فوق البار (Muscle-up)"
        "Handstand" -> "الوقوف على اليدين (Handstand)"
        "Front Lever" -> "الثبات الأمامي المعلّق (Front Lever)"
        "Planche" -> "الثبات الأفقي على اليدين (Planche)"
        "L-sit" -> "الثبات في وضعية L (L-sit)"
        "Pistol Squat" -> "القرفصاء بساق واحدة (Pistol Squat)"
        else -> value
    }

    fun equipment(value: String): String = when (value) {
        "Pull-up Bar" -> "بار السحب"
        "Parallel Bars" -> "جهاز المتوازي"
        "Rings" -> "الحلقات"
        "Resistance Band" -> "شريط مقاومة"
        "Bench" -> "مقعد تدريبي"
        else -> value
    }

    fun category(value: String): String = when (value) {
        "Chest" -> "الصدر"
        "Back" -> "الظهر"
        "Legs" -> "الساقان"
        "Core" -> "الجذع"
        "Cardio" -> "اللياقة القلبية"
        "Mobility" -> "المرونة والحركة"
        "Arms & Shoulders" -> "الذراعان والكتفان"
        else -> value
    }

    fun exercise(value: String): String = when (value) {
        "Air Squat" -> "قرفصاء بوزن الجسم"
        "Alternating Superman" -> "رفع الذراع والساق المتعاكسين"
        "Bench Dip" -> "ضغط خلفي على المقعد (Bench Dip)"
        "Bench Kneeling Lat Stretch" -> "إطالة العضلات الظهرية على المقعد"
        "Bicycle Crunch" -> "تمرين الدراجة للبطن"
        "Body Saw" -> "بلانك متحرك أمامًا وخلفًا"
        "Bulgarian Split Squat" -> "القرفصاء البلغارية"
        "Burpee" -> "تمرين بيربي (Burpee)"
        "Butt Kicks" -> "ركلات الكعب للخلف"
        "Calf Raise" -> "رفع الكعبين"
        "Cat Cow Stretch" -> "إطالة القطة والبقرة"
        "Chest Dip" -> "ضغط على المتوازي للصدر"
        "Child's Pose Back Stretch" -> "وضعية الطفل لإطالة الظهر"
        "Crunch" -> "انقباض البطن (Crunch)"
        "Dead Bug" -> "تمرين الجذع المتعاكس"
        "Decline Push Up" -> "ضغط مع رفع القدمين"
        "Deficit Push Up" -> "ضغط بمدى حركي عميق"
        "Doorway Chest Stretch" -> "إطالة الصدر عند الباب"
        "Downward Facing Dog" -> "وضعية الكلب المتجه لأسفل"
        "High Knee Taps" -> "رفع الركبتين عاليًا"
        "Hip Flexor Stretch" -> "إطالة مثنيات الورك"
        "Hip Thrust" -> "دفع الورك"
        "Incline Push Up" -> "ضغط مائل مع رفع الجذع"
        "Inverted Row" -> "سحب أفقي مقلوب"
        "Jump Rope" -> "نط الحبل"
        "Jumping Jack" -> "فتح وضم الذراعين والساقين (Jumping Jack)"
        "Kneeling Hamstring Stretch" -> "إطالة أوتار الركبة من الركوع"
        "Lunging Calf Stretch" -> "إطالة عضلة الساق بوضعية الاندفاع"
        "Lying Back Extension" -> "تمديد الظهر أرضًا"
        "Lying Glute Stretch" -> "إطالة عضلات الألوية أرضًا"
        "Overhead Tricep Stretch" -> "إطالة العضلة ثلاثية الرؤوس فوق الرأس"
        "Parallel Grip Pull Up" -> "سحب على البار بقبضة متوازية"
        "Pike Push Up" -> "ضغط الكتفين بوضعية V مقلوبة"
        "Plank Shoulder Tap" -> "لمس الكتفين من وضعية البلانك"
        "Pseudo Push Up" -> "ضغط مائل للأمام"
        "Rear Deltoid Stretch" -> "إطالة الكتف الخلفي"
        "Regular Pull Up" -> "سحب على البار"
        "Regular Push Up" -> "تمرين الضغط"
        "Reverse Crunch" -> "رفع الحوض للبطن (Reverse Crunch)"
        "Reverse Hyperextension" -> "تمديد الورك العكسي"
        "Running in Place Punches" -> "جري في المكان مع لكمات"
        "Running in Place" -> "جري في المكان"
        "SIngle Leg Deadlift", "Single Leg Deadlift" -> "الرفعة الميتة بساق واحدة"
        "Side Crunch" -> "انقباض جانبي للبطن"
        "Side Lunge Stretch" -> "إطالة الاندفاع الجانبي"
        "Side Tilt" -> "ميل جانبي للجذع"
        "Sit Up" -> "الجلوس من الاستلقاء (Sit-up)"
        "Split Squat" -> "قرفصاء بوضعية الاندفاع الثابت"
        "Standing Forward Bending" -> "انحناء أمامي من الوقوف"
        "Standing Knee to Chest Stretch" -> "سحب الركبة إلى الصدر من الوقوف"
        "Standing Quadricep Stretch" -> "إطالة العضلة الأمامية للفخذ"
        "Sumo Squat" -> "قرفصاء واسعة (Sumo)"
        "Tricep Extension" -> "تمديد العضلة ثلاثية الرؤوس"
        "Upward Facing Dog" -> "وضعية الكلب المتجه لأعلى"
        "Hollow Body Hold" -> "الثبات المجوف للجذع"
        "Passive Hang" -> "التعلّق بالبار"
        else -> value
    }
}

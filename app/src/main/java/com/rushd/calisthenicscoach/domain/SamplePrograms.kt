package com.rushd.calisthenicscoach.domain

object SamplePrograms {
    val foundation = WorkoutPlan(
        id = "foundation_a",
        title = "Foundation A",
        subtitle = "دفع + سحب + جذع",
        level = "مبتدئ",
        durationMin = 38,
        exercises = listOf(
            Exercise("Incline Push-up", "ضغط مائل", 4, "8–12", 75, "حافظ على خط مستقيم للجسم"),
            Exercise("Body Row", "سحب أفقي", 4, "6–10", 90, "اسحب الصدر باتجاه البار"),
            Exercise("Split Squat", "قرفصاء منفصلة", 3, "8/جهة", 60, "ركبة ثابتة ومسار متحكم"),
            Exercise("Dead Bug", "ديد باغ", 3, "8/جهة", 45, "ثبت أسفل الظهر"),
            Exercise("Passive Hang", "تعلّق سلبي", 3, "20–30 ث", 60, "قبضة ثابتة وتنفس هادئ")
        )
    )

    val strength = WorkoutPlan(
        id = "strength_b",
        title = "Strength B",
        subtitle = "قوة أساسية للـ Pull-up والـ Dip",
        level = "متوسط",
        durationMin = 50,
        exercises = listOf(
            Exercise("Pull-up", "عقلة", 5, "3–6", 120, "ابدأ بلوح كتف نشط"),
            Exercise("Parallel Bar Dip", "متوازي", 5, "4–8", 120, "كتف منخفض وجذع ثابت"),
            Exercise("Pistol Box Squat", "بستول إلى صندوق", 4, "5/جهة", 90, "تحكم كامل في النزول"),
            Exercise("Hollow Hold", "هولو هولد", 4, "20–40 ث", 60, "الأضلاع للأسفل والحوض ملتف"),
            Exercise("Scapular Pull-up", "عقلة لوح الكتف", 3, "8–12", 60, "حركة من لوحي الكتف")
        )
    )

    val skills = listOf(
        Skill("Pull-up", "متوسط", 70, "6–8 تكرارات نظيفة"),
        Skill("Handstand", "أساسي", 42, "Wall toe pulls"),
        Skill("Muscle-up", "تمهيدي", 25, "Chest-to-bar explosiveness"),
        Skill("L-sit", "متوسط", 58, "20 ثانية متصلة")
    )
}

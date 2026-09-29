package com.rushd.calisthenicscoach.domain

import kotlin.math.roundToInt

object ProgramEngine {

    fun build(
        profile: UserProfile,
        effortHistory: Map<String, Double> = emptyMap()
    ): TrainingBlueprint {
        val level = level(profile)
        val recovery = recoveryProfile(profile)
        val totalWeeks = when (profile.goal) {
            "Muscle-up", "Front Lever", "Planche", "Handstand" -> 8
            "Pull-up" -> 6
            else -> 6
        }

        val plans = buildPlans(profile, level, recovery)
            .map { plan ->
                plan.copy(
                    exercises = plan.exercises
                        .filter { ExerciseVideoCatalog.forExercise(it.name) != null }
                        .map { exercise -> adaptFromHistory(exercise, effortHistory[exercise.name]) }
                )
            }
            .filter { it.exercises.isNotEmpty() }

        val trainingDays = distributeDays(profile.daysPerWeek.coerceIn(2, 5))
        val weekly = trainingDays.mapIndexed { index, day ->
            val plan = plans[index % plans.size]
            ProgramDay(
                dayNumber = day,
                title = plan.title,
                subtitle = plan.subtitle,
                kind = "workout",
                planId = plan.id
            )
        }

        val notes = buildList {
            if (profile.age >= 50) add("تم تخفيف حجم الجلسة وزيادة فترات الراحة بما يناسب الاستشفاء.")
            if (profile.activityLevel == "منخفض") add("بدأت الخطة بتدرج محافظ لرفع القدرة على الانتظام.")
            if (profile.limitations.isNotEmpty()) add("تم تجنب الحركات الأكثر تعارضًا مع القيود التي حددتها.")
            if (bodyLoadIndex(profile) >= 30.0) add("تم تفضيل نسخ أقل صدمة وأكثر قابلية للتحكم في البداية.")
        }

        return TrainingBlueprint(
            title = "${goalArabic(profile.goal)} · $level",
            phaseTitle = phaseTitle(profile, level),
            week = 1,
            totalWeeks = totalWeeks,
            progress = 1f / totalWeeks,
            weeklyDays = weekly,
            rationale = rationale(profile, level, recovery),
            plans = plans,
            readinessNotes = notes
        )
    }

    private fun buildPlans(
        profile: UserProfile,
        level: String,
        recovery: RecoveryProfile
    ): List<WorkoutPlan> {
        val foundation = WorkoutPlan(
            id = "adaptive_foundation",
            title = "تأسيس الحركة",
            subtitle = "قوة أساسية · تحكم · جذع",
            level = level,
            durationMin = profile.sessionMinutes,
            exercises = adapt(
                listOf(
                    ex("Incline Push Up", "ضغط مائل", 3, "8–12", 60, "جسم مستقيم ونزول متحكم"),
                    ex(backChoice(profile), backArabic(backChoice(profile)), 3, "6–10", 75, "اسحب من لوح الكتف وحافظ على الجذع ثابتًا"),
                    ex(legChoice(profile), legArabic(legChoice(profile)), 3, "8/جهة", 60, "تحكم في النزول وحافظ على الركبة مستقرة"),
                    ex("Dead Bug", "ديد باغ", 3, "8/جهة", 45, "ثبت أسفل الظهر وحرّك الأطراف ببطء"),
                    ex("Hip Flexor Stretch", "إطالة مثنيات الورك", 2, "30 ث/جهة", 30, "تنفس بهدوء وتجنب الدفع المؤلم")
                ),
                recovery
            )
        )

        val strength = WorkoutPlan(
            id = "adaptive_strength",
            title = if (profile.goal in setOf("Pull-up", "Muscle-up", "Front Lever")) "قوة السحب" else "قوة الجسم الكامل",
            subtitle = goalSubtitle(profile.goal),
            level = level,
            durationMin = profile.sessionMinutes,
            exercises = adapt(
                strengthExercises(profile, level),
                recovery
            )
        )

        val skill = WorkoutPlan(
            id = "adaptive_skill",
            title = "مهارة وتحكم",
            subtitle = skillSubtitle(profile),
            level = level,
            durationMin = (profile.sessionMinutes * 0.8f).roundToInt().coerceAtLeast(25),
            exercises = adapt(
                skillExercises(profile),
                recovery.copy(setReduction = 0)
            )
        )

        return when (profile.daysPerWeek.coerceIn(2, 5)) {
            2 -> listOf(foundation, strength)
            3 -> listOf(foundation, strength, skill)
            4 -> listOf(foundation, strength, foundation.copy(id = "adaptive_foundation_b"), skill)
            else -> listOf(foundation, strength, foundation.copy(id = "adaptive_foundation_b"), skill, strength.copy(id = "adaptive_strength_b"))
        }
    }

    private fun strengthExercises(profile: UserProfile, level: String): List<Exercise> {
        val pull = backChoice(profile)

        val push = when {
            "ألم/انزعاج في الكتف" in profile.limitations -> "Regular Push Up"
            "Parallel Bars" in profile.equipment && profile.maxDips >= 3 -> "Chest Dip"
            profile.maxPushUps >= 15 -> "Pseudo Push Up"
            else -> "Regular Push Up"
        }

        val leg = if ("ألم/انزعاج في الركبة" in profile.limitations) "Hip Thrust" else "Bulgarian Split Squat"
        val shoulder = if ("ألم/انزعاج في المعصم" in profile.limitations) "Plank Shoulder Tap" else "Pike Push Up"

        return listOf(
            ex(
                pull,
                backArabic(pull),
                if (level == "متوسط") 4 else 3,
                when {
                    pull.contains("Pull Up") -> "3–6"
                    pull == "Alternating Superman" -> "8/جهة"
                    else -> "6–10"
                },
                90,
                "ابدأ الحركة بتحكم وتوقف قبل فقدان الوضعية"
            ),
            ex(push, pushArabic(push), if (level == "متوسط") 4 else 3, "6–10", 90, "حافظ على الكتف منخفضًا والجذع ثابتًا"),
            ex(leg, legArabic(leg), 3, if (leg == "Hip Thrust") "10–15" else "8/جهة", 75, "حركة كاملة دون فقدان التحكم"),
            ex(shoulder, shoulderArabic(shoulder), 3, "6–10", 75, "أوقف المجموعة قبل انهيار التقنية"),
            ex("Reverse Crunch", "كرنش عكسي", 3, "10–15", 45, "حرّك الحوض بدل رمي الساقين")
        )
    }

    private fun skillExercises(profile: UserProfile): List<Exercise> {
        val goal = profile.goal
        return when (goal) {
            "Handstand", "Planche" -> listOf(
                ex("Pike Push Up", "ضغط بايك", 3, "6–10", 75, "ادفع الأرض وحافظ على الكتفين نشطين"),
                ex("Plank Shoulder Tap", "لمس الكتف من البلانك", 3, "8/جهة", 45, "قلل دوران الحوض"),
                ex("Body Saw", "بودي سو", 3, "8–12", 45, "حافظ على الجذع مشدودًا"),
                ex("Downward Facing Dog", "وضعية الكلب لأسفل", 2, "30–40 ث", 30, "تنفس ببطء وأطل الكتفين")
            )
            "Pull-up", "Muscle-up", "Front Lever" -> listOf(
                ex(backChoice(profile), backArabic(backChoice(profile)), 4, "4–8", 90, "ركز على سحب لوحي الكتف أولًا"),
                ex(backAccessory(profile), backArabic(backAccessory(profile)), 3, if (backAccessory(profile) == "Alternating Superman") "8/جهة" else "8–12", 60, "حافظ على الجذع ثابتًا والحركة متحكمًا بها"),
                ex("Alternating Superman", "سوبرمان متبادل", 3, "8/جهة", 45, "ارفع بمدى صغير ومتحكم"),
                ex("Body Saw", "بودي سو", 3, "8–12", 45, "ثبّت الجذع طوال الحركة")
            )
            else -> listOf(
                ex("Pike Push Up", "ضغط بايك", 3, "6–10", 60, "حافظ على تحكم كامل"),
                ex(backAccessory(profile), backArabic(backAccessory(profile)), 3, if (backAccessory(profile) == "Alternating Superman") "8/جهة" else "8–12", 60, "نفذ الحركة ببطء ومن دون اندفاع"),
                ex("Bicycle Crunch", "كرنش الدراجة", 3, "10/جهة", 45, "تحرك ببطء دون شد الرقبة"),
                ex("Cat Cow Stretch", "إطالة القطة والبقرة", 2, "8–10", 30, "نسق الحركة مع التنفس")
            )
        }
    }

    private fun adaptFromHistory(exercise: Exercise, averageRpe: Double?): Exercise {
        if (averageRpe == null) return exercise
        return when {
            averageRpe >= 9.0 -> exercise.copy(
                sets = (exercise.sets - 1).coerceAtLeast(2),
                restSec = (exercise.restSec + 20).coerceAtMost(180)
            )
            averageRpe <= 6.0 -> exercise.copy(
                sets = (exercise.sets + 1).coerceAtMost(5),
                restSec = (exercise.restSec - 10).coerceAtLeast(30)
            )
            else -> exercise
        }
    }

    private fun adapt(items: List<Exercise>, recovery: RecoveryProfile): List<Exercise> =
        items.map { exercise ->
            exercise.copy(
                sets = (exercise.sets - recovery.setReduction).coerceAtLeast(2),
                restSec = exercise.restSec + recovery.extraRestSec
            )
        }

    private fun backChoice(profile: UserProfile): String = when {
        "Pull-up Bar" in profile.equipment && profile.maxPullUps >= 3 -> "Regular Pull Up"
        "Pull-up Bar" in profile.equipment -> "Parallel Grip Pull Up"
        "Rings" in profile.equipment -> "Inverted Row"
        else -> "Alternating Superman"
    }

    private fun backAccessory(profile: UserProfile): String = when {
        "Rings" in profile.equipment || "Pull-up Bar" in profile.equipment -> "Inverted Row"
        "ألم/انزعاج أسفل الظهر" in profile.limitations -> "Cat Cow Stretch"
        else -> "Alternating Superman"
    }

    private fun legChoice(profile: UserProfile): String =
        if ("ألم/انزعاج في الركبة" in profile.limitations || bodyLoadIndex(profile) >= 30.0)
            "Hip Thrust"
        else
            "Split Squat"

    private fun level(profile: UserProfile): String = when {
        profile.maxPullUps >= 8 && profile.maxDips >= 10 && profile.maxPushUps >= 25 -> "متوسط"
        profile.maxPullUps >= 3 || profile.maxPushUps >= 15 -> "مبتدئ متقدم"
        else -> "تأسيسي"
    }

    private fun recoveryProfile(profile: UserProfile): RecoveryProfile {
        var reduction = 0
        var rest = 0
        if (profile.age >= 50) {
            reduction += 1
            rest += 20
        } else if (profile.age >= 40) {
            rest += 10
        }
        if (profile.activityLevel == "منخفض") rest += 10
        if (profile.limitations.isNotEmpty()) rest += 10
        return RecoveryProfile(reduction.coerceAtMost(1), rest.coerceAtMost(30))
    }

    private fun distributeDays(days: Int): List<Int> = when (days) {
        2 -> listOf(1, 4)
        3 -> listOf(1, 3, 5)
        4 -> listOf(1, 3, 5, 6)
        else -> listOf(1, 2, 4, 5, 6)
    }

    private fun bodyLoadIndex(profile: UserProfile): Double {
        val h = profile.heightCm.coerceAtLeast(120) / 100.0
        return profile.weightKg.coerceAtLeast(35) / (h * h)
    }

    private fun phaseTitle(profile: UserProfile, level: String): String = when {
        level == "تأسيسي" -> "مرحلة التأسيس والتحكم"
        profile.goal in setOf("Muscle-up", "Front Lever", "Planche", "Handstand") -> "مرحلة القوة الخاصة بالمهارة"
        else -> "مرحلة بناء القوة"
    }

    private fun rationale(profile: UserProfile, level: String, recovery: RecoveryProfile): String {
        val eq = if (profile.equipment.isEmpty()) "وزن الجسم" else profile.equipment.joinToString("، ") { equipmentArabic(it) }
        return "بُنيت الخطة لعمر ${profile.age} سنة، مستوى $level، نشاط ${profile.activityLevel}، ${profile.daysPerWeek} أيام أسبوعيًا، و${profile.sessionMinutes} دقيقة للجلسة. الهدف: ${goalArabic(profile.goal)}. المعدات: $eq."
    }

    private fun goalSubtitle(goal: String): String = when (goal) {
        "Pull-up" -> "سحب · ظهر · جذع"
        "Muscle-up" -> "سحب قوي · دفع · جذع"
        "Front Lever" -> "ظهر · جذع · تحكم"
        "Handstand" -> "كتف · دفع · ثبات"
        "Planche" -> "دفع · كتف · جذع"
        else -> "دفع · سحب · أرجل · جذع"
    }

    private fun skillSubtitle(profile: UserProfile): String =
        if (profile.targetSkills.isEmpty()) "قوة نوعية · تحكم · مرونة"
        else profile.targetSkills.take(2).joinToString(" + ") { goalArabic(it) }

    private fun goalArabic(value: String): String = when (value) {
        "Pull-up" -> "العقلة"
        "Muscle-up" -> "المسل أب"
        "Handstand" -> "الوقوف على اليدين"
        "Front Lever" -> "الفرونت ليفر"
        "Planche" -> "البلانش"
        "L-sit" -> "إل-سِت"
        else -> value
    }

    private fun equipmentArabic(value: String): String = when (value) {
        "Pull-up Bar" -> "بار العقلة"
        "Parallel Bars" -> "المتوازي"
        "Rings" -> "الحلقات"
        "Resistance Band" -> "حزام مقاومة"
        "Bench" -> "مقعد"
        else -> value
    }

    private fun ex(name: String, ar: String, sets: Int, reps: String, rest: Int, cue: String) =
        Exercise(name, ar, sets, reps, rest, cue)

    private fun backArabic(name: String) = when (name) {
        "Regular Pull Up" -> "عقلة"
        "Parallel Grip Pull Up" -> "عقلة بقبضة متوازية"
        "Inverted Row" -> "سحب أفقي"
        "Alternating Superman" -> "سوبرمان متبادل"
        "Cat Cow Stretch" -> "إطالة القطة والبقرة"
        else -> "تمرين ظهر"
    }

    private fun pushArabic(name: String) = when (name) {
        "Chest Dip" -> "ديبس للصدر"
        "Pseudo Push Up" -> "ضغط سودو"
        "Regular Push Up" -> "ضغط"
        else -> "تمرين دفع"
    }

    private fun legArabic(name: String) = when (name) {
        "Bulgarian Split Squat" -> "قرفصاء بلغارية"
        "Split Squat" -> "قرفصاء منفصلة"
        "Hip Thrust" -> "دفع الورك"
        else -> "تمرين أرجل"
    }

    private fun shoulderArabic(name: String) = when (name) {
        "Pike Push Up" -> "ضغط بايك"
        "Plank Shoulder Tap" -> "لمس الكتف من البلانك"
        else -> "تمرين كتف"
    }

    private data class RecoveryProfile(val setReduction: Int, val extraRestSec: Int)
}

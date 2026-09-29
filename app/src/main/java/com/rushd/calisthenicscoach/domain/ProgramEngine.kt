package com.rushd.calisthenicscoach.domain

object ProgramEngine {
    fun build(profile: UserProfile): TrainingBlueprint {
        val level = when {
            profile.maxPullUps >= 8 && profile.maxDips >= 12 -> "متوسط"
            profile.maxPushUps >= 15 || profile.maxPullUps >= 3 -> "مبتدئ متقدم"
            else -> "تأسيسي"
        }

        val totalWeeks = when (profile.goal) {
            "Muscle-up", "Front Lever", "Planche", "Handstand" -> 8
            else -> 6
        }

        val sessions = when (profile.daysPerWeek.coerceIn(2, 5)) {
            2 -> listOf(
                ProgramDay(1, "Foundation A", "دفع + سحب + أرجل + جذع", "workout", SamplePrograms.foundation.id),
                ProgramDay(4, "Strength B", "قوة أساسية + تحكم", "workout", SamplePrograms.strength.id)
            )
            3 -> listOf(
                ProgramDay(1, "Foundation A", "قاعدة شاملة وتقنية", "workout", SamplePrograms.foundation.id),
                ProgramDay(3, "Strength B", "قوة السحب والدفع", "workout", SamplePrograms.strength.id),
                ProgramDay(5, "Foundation A", "تثبيت التقنية والحجم", "workout", SamplePrograms.foundation.id)
            )
            4 -> listOf(
                ProgramDay(1, "Foundation A", "قاعدة شاملة وتقنية", "workout", SamplePrograms.foundation.id),
                ProgramDay(3, "Strength B", "قوة السحب والدفع", "workout", SamplePrograms.strength.id),
                ProgramDay(5, "Foundation A", "تثبيت التقنية والحجم", "workout", SamplePrograms.foundation.id),
                ProgramDay(6, "Skill Practice", skillSubtitle(profile), "skill")
            )
            else -> listOf(
                ProgramDay(1, "Foundation A", "قاعدة شاملة وتقنية", "workout", SamplePrograms.foundation.id),
                ProgramDay(2, "Mobility", "مرونة واستشفاء نشط", "mobility"),
                ProgramDay(3, "Strength B", "قوة السحب والدفع", "workout", SamplePrograms.strength.id),
                ProgramDay(5, "Foundation A", "تثبيت التقنية والحجم", "workout", SamplePrograms.foundation.id),
                ProgramDay(6, "Skill Practice", skillSubtitle(profile), "skill")
            )
        }

        return TrainingBlueprint(
            title = "${profile.goal} · $level",
            phaseTitle = if (level == "تأسيسي") "مرحلة التأسيس" else "مرحلة القوة والتحكم",
            week = 1,
            totalWeeks = totalWeeks,
            progress = 0.08f,
            weeklyDays = sessions,
            rationale = rationale(profile, level)
        )
    }

    private fun skillSubtitle(profile: UserProfile): String =
        if (profile.targetSkills.isEmpty()) "مهارة مختارة + جذع"
        else profile.targetSkills.take(2).joinToString(" + ")

    private fun rationale(profile: UserProfile, level: String): String {
        val equipment = if (profile.equipment.isEmpty()) "وزن الجسم فقط" else profile.equipment.joinToString("، ")
        return "الخطة مبنية على مستوى $level، ${profile.daysPerWeek} أيام أسبوعيًا، ${profile.sessionMinutes} دقيقة للجلسة، ومعدات: $equipment."
    }
}

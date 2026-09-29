package com.rushd.calisthenicscoach.domain

data class UserProfile(
    val age: Int = 30,
    val heightCm: Int = 175,
    val weightKg: Int = 75,
    val activityLevel: String = "متوسط",
    val goal: String = "قوة عامة",
    val experience: String = "مبتدئ",
    val daysPerWeek: Int = 3,
    val sessionMinutes: Int = 45,
    val equipment: Set<String> = emptySet(),
    val limitations: Set<String> = emptySet(),
    val targetSkills: Set<String> = emptySet(),
    val maxPushUps: Int = 0,
    val maxPullUps: Int = 0,
    val maxDips: Int = 0,
    val hollowHoldSec: Int = 0,
    val onboardingCompleted: Boolean = false
)

data class ProgramDay(
    val dayNumber: Int,
    val title: String,
    val subtitle: String,
    val kind: String,
    val planId: String? = null
)

data class TrainingBlueprint(
    val title: String,
    val phaseTitle: String,
    val week: Int,
    val totalWeeks: Int,
    val progress: Float,
    val weeklyDays: List<ProgramDay>,
    val rationale: String,
    val plans: List<WorkoutPlan>,
    val readinessNotes: List<String> = emptyList()
)

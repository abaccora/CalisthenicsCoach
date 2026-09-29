package com.rushd.calisthenicscoach.domain

data class Exercise(
    val name: String,
    val ArabicName: String,
    val sets: Int,
    val reps: String,
    val restSec: Int,
    val cue: String
)

data class WorkoutPlan(
    val id: String,
    val title: String,
    val subtitle: String,
    val level: String,
    val durationMin: Int,
    val exercises: List<Exercise>
)

data class Skill(
    val name: String,
    val level: String,
    val progress: Int,
    val nextStep: String
)

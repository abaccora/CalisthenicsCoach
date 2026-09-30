package com.rushd.calisthenicscoach.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailySessionAdapterTest {

    private val plan = WorkoutPlan(
        id = "test",
        title = "جلسة",
        subtitle = "اختبار",
        level = "تأسيسي",
        durationMin = 45,
        exercises = listOf(
            Exercise("Regular Push Up", "ضغط", 3, "8–12", 60, ""),
            Exercise("Regular Pull Up", "سحب على البار", 3, "5", 90, ""),
            Exercise("Split Squat", "قرفصاء بوضعية الاندفاع الثابت", 3, "8/جهة", 60, ""),
            Exercise("Dead Bug", "تمرين الجذع المتعاكس (Dead Bug)", 3, "8/جهة", 45, "")
        )
    )

    @Test
    fun lowEnergyReducesVolumeAndAddsRest() {
        val adapted = DailySessionAdapter.adapt(
            plan,
            DailyReadiness(
                energy = EnergyLevel.LOW,
                minutesAvailable = 45,
                equipmentAvailable = setOf("Pull-up Bar")
            )
        )

        assertEquals(2, adapted.exercises.first().sets)
        assertEquals(75, adapted.exercises.first().restSec)
    }

    @Test
    fun shortSessionKeepsOnlyPriorityExercises() {
        val adapted = DailySessionAdapter.adapt(
            plan,
            DailyReadiness(
                energy = EnergyLevel.NORMAL,
                minutesAvailable = 20,
                equipmentAvailable = setOf("Pull-up Bar")
            )
        )

        assertTrue(adapted.exercises.size < plan.exercises.size)
        assertTrue(adapted.exercises.size >= 2)
        assertEquals(20, adapted.durationMin)
    }
}

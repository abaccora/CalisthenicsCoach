package com.rushd.calisthenicscoach.data.source

import com.rushd.calisthenicscoach.data.SourceActionEntity

/**
 * Index extracted from the user-provided Home Workout library.
 *
 * The full source contains 814 Action records. We keep the raw source index
 * separate from the curated Coach exercise catalog: indexing is lossless,
 * promotion into ExerciseEntity is deliberate and quality-controlled.
 */
object HomeWorkoutSourceIndex {
    const val SOURCE = "HOME_WORKOUT_LIBRARY"
    const val EXPECTED_ACTION_COUNT = 814
    const val EXPECTED_EMBEDDED_VIDEO_COUNT = 49

    private val ranges = listOf(
        0..1,
        3..4,
        6..10,
        12..13,
        17..18,
        20..23,
        25..32,
        34..39,
        42..42,
        45..52,
        55..55,
        59..59,
        61..63,
        65..65,
        67..67,
        70..71,
        73..75,
        82..82,
        85..85,
        88..89,
        91..94,
        120..120,
        124..125,
        127..127,
        129..130,
        149..149,
        155..155,
        163..165,
        167..169,
        175..175,
        177..178,
        183..183,
        185..188,
        190..193,
        195..195,
        223..223,
        226..231,
        233..236,
        238..238,
        244..246,
        263..263,
        265..265,
        274..274,
        278..279,
        282..282,
        287..287,
        298..299,
        301..301,
        328..329,
        355..355,
        369..369,
        377..381,
        387..388,
        428..428,
        439..439,
        445..446,
        454..455,
        468..469,
        477..479,
        493..493,
        509..510,
        541..541,
        549..550,
        562..563,
        565..566,
        588..589,
        736..739,
        791..793,
        798..798,
        948..948,
        951..951,
        2823..2833,
        2836..2972,
        10000..10050,
        10052..10298,
        10300..10409,
        10411..10505,
        10507..10513
    )

    val actionIds: List<Int> by lazy {
        ranges.asSequence().flatMap { it.asSequence() }.toList()
    }

    val embeddedVideoActionIds: Set<Int> = setOf(
        0, 17, 21, 22, 48, 61, 62, 65, 120, 127, 129, 130, 149, 155, 163,
        167, 177, 187, 238, 263, 298, 299, 328, 329, 355, 387, 388, 477, 478,
        2836, 2839, 2840, 2847, 2848, 2849, 2850, 2860, 2861, 2872, 2873,
        2895, 2896, 2914, 2925, 2932, 2941, 2947, 2957, 2968
    )

    fun asEntities(): List<SourceActionEntity> = actionIds.map { actionId ->
        SourceActionEntity(
            source = SOURCE,
            sourceActionId = actionId,
            hasArabic = true,
            hasEnglish = true,
            hasEmbeddedVideo = actionId in embeddedVideoActionIds,
            mediaKey = actionId.takeIf { it in embeddedVideoActionIds }
                ?.let { "homework_action_$it" }
        )
    }
}

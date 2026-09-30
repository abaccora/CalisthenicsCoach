package com.rushd.calisthenicscoach.data.source

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeWorkoutSourceIndexTest {

    @Test
    fun sourceIndexMatchesExtractedLibrary() {
        val ids = HomeWorkoutSourceIndex.actionIds
        assertEquals(HomeWorkoutSourceIndex.EXPECTED_ACTION_COUNT, ids.size)
        assertEquals(ids.size, ids.toSet().size)
        assertEquals(HomeWorkoutSourceIndex.EXPECTED_EMBEDDED_VIDEO_COUNT, HomeWorkoutSourceIndex.embeddedVideoActionIds.size)
        assertTrue(HomeWorkoutSourceIndex.embeddedVideoActionIds.all { it in ids })
    }

    @Test
    fun sourceEntitiesExposeArabicEnglishAndVideoFlags() {
        val entities = HomeWorkoutSourceIndex.asEntities()
        assertEquals(814, entities.size)
        assertEquals(49, entities.count { it.hasEmbeddedVideo })
        assertTrue(entities.all { it.hasArabic && it.hasEnglish })
        assertTrue(entities.filter { it.hasEmbeddedVideo }.all { !it.mediaKey.isNullOrBlank() })
    }
}

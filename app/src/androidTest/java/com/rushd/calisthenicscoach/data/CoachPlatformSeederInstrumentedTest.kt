package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.rushd.calisthenicscoach.data.source.HomeWorkoutSourceIndex
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CoachPlatformSeederInstrumentedTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CoachPlatformDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.coachPlatformDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedFoundationIndexesFullSourceAndPersistsCoreCoachGraph() = runBlocking {
        CoachPlatformSeeder(dao).seedFoundation()

        assertEquals(814, dao.countSourceActions(HomeWorkoutSourceIndex.SOURCE))
        assertEquals(49, dao.getSourceActionsWithVideo(HomeWorkoutSourceIndex.SOURCE).size)
        assertTrue(dao.countCoreExercises() >= 54)
        assertEquals(8, dao.countSkills())
        assertTrue(dao.countProgressionNodes() >= 30)
        assertTrue(dao.countProgressionEdges() >= 20)
    }
}

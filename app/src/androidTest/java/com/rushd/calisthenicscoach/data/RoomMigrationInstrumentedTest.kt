package com.rushd.calisthenicscoach.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomMigrationInstrumentedTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val databaseName = "calisthenics-migration-test.db"

    @After
    fun cleanUp() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration1To3PreservesHistoryAndCreatesCoachPlatformTables() = runBlocking {
        context.deleteDatabase(databaseName)
        val file = context.getDatabasePath(databaseName)
        file.parentFile?.mkdirs()

        SQLiteDatabase.openOrCreateDatabase(file, null).use { legacy ->
            legacy.execSQL(
                """
                CREATE TABLE IF NOT EXISTS workout_history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    planId TEXT NOT NULL,
                    title TEXT NOT NULL,
                    completedAt INTEGER NOT NULL,
                    durationSec INTEGER NOT NULL,
                    completedExercises INTEGER NOT NULL
                )
                """.trimIndent()
            )
            legacy.execSQL(
                """
                INSERT INTO workout_history
                    (planId, title, completedAt, durationSec, completedExercises)
                VALUES (?, ?, ?, ?, ?)
                """.trimIndent(),
                arrayOf("foundation_a", "جلسة تأسيسية", 1000L, 1200, 4)
            )
            legacy.version = 1
        }

        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            databaseName
        )
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

        try {
            val history = database.workoutDao().observeAll().first()
            assertEquals(1, history.size)
            assertEquals("foundation_a", history.single().planId)
            assertEquals("جلسة تأسيسية", history.single().title)

            val sql = database.openHelper.writableDatabase
            listOf(
                "workout_sessions",
                "workout_substitutions",
                "set_logs",
                "assessment_results",
                "skill_states",
                "athlete_profiles",
                "assessments",
                "source_actions",
                "exercises",
                "exercise_variants",
                "skills",
                "progression_nodes",
                "progression_edges",
                "training_programs",
                "program_weeks",
                "workouts",
                "workout_exercises",
                "readiness_checks",
                "performance_history",
                "personal_bests"
            ).forEach { table ->
                assertTableExists(sql, table)
            }
        } finally {
            database.close()
        }
    }

    private fun assertTableExists(db: SupportSQLiteDatabase, table: String) {
        db.query(
            "SELECT name FROM sqlite_master WHERE type = 'table' AND name = ?",
            arrayOf(table)
        ).use { cursor ->
            assertTrue("Expected table $table after migration", cursor.moveToFirst())
        }
    }
}

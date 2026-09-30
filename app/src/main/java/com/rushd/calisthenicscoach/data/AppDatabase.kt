package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        WorkoutHistory::class,
        WorkoutSessionEntity::class,
        WorkoutSubstitutionEntity::class,
        SetLogEntity::class,
        AssessmentResultEntity::class,
        SkillStateEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun coachDao(): CoachDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_sessions (
                        id TEXT NOT NULL PRIMARY KEY,
                        planId TEXT NOT NULL,
                        startedAt INTEGER NOT NULL,
                        endedAt INTEGER,
                        status TEXT NOT NULL,
                        currentExerciseIndex INTEGER NOT NULL,
                        perceivedDifficulty TEXT,
                        painReported INTEGER NOT NULL DEFAULT 0
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_substitutions (
                        sessionId TEXT NOT NULL,
                        exerciseIndex INTEGER NOT NULL,
                        replacementName TEXT NOT NULL,
                        PRIMARY KEY(sessionId, exerciseIndex),
                        FOREIGN KEY(sessionId) REFERENCES workout_sessions(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_substitutions_sessionId ON workout_substitutions(sessionId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS set_logs (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        sessionId TEXT NOT NULL,
                        exerciseIndex INTEGER NOT NULL,
                        exerciseName TEXT NOT NULL,
                        setIndex INTEGER NOT NULL,
                        reps INTEGER,
                        holdSeconds INTEGER,
                        leftReps INTEGER,
                        rightReps INTEGER,
                        assistance TEXT,
                        addedWeightKg REAL,
                        bandLevel TEXT,
                        tempo TEXT,
                        rpe INTEGER,
                        rir INTEGER,
                        completed INTEGER NOT NULL,
                        loggedAt INTEGER NOT NULL,
                        FOREIGN KEY(sessionId) REFERENCES workout_sessions(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_set_logs_sessionId ON set_logs(sessionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_set_logs_exerciseName ON set_logs(exerciseName)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_set_logs_sessionId_exerciseIndex_setIndex ON set_logs(sessionId, exerciseIndex, setIndex)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS assessment_results (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        metricId TEXT NOT NULL,
                        value REAL NOT NULL,
                        unit TEXT NOT NULL,
                        goalContext TEXT NOT NULL,
                        recordedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS skill_states (
                        skillId TEXT NOT NULL PRIMARY KEY,
                        currentNodeId TEXT NOT NULL,
                        masteryScore REAL NOT NULL,
                        successfulSessions INTEGER NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "calisthenics.db"
            )
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}

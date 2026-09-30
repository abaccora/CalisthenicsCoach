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
        SkillStateEntity::class,
        AthleteProfileEntity::class,
        AssessmentEntity::class,
        SourceActionEntity::class,
        ExerciseEntity::class,
        ExerciseVariantEntity::class,
        SkillEntity::class,
        ProgressionNodeEntity::class,
        ProgressionEdgeEntity::class,
        TrainingProgramEntity::class,
        ProgramWeekEntity::class,
        WorkoutEntity::class,
        WorkoutExerciseEntity::class,
        ReadinessCheckEntity::class,
        PerformanceHistoryEntity::class,
        PersonalBestEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun workoutDao(): WorkoutDao
    abstract fun coachDao(): CoachDao
    abstract fun coachPlatformDao(): CoachPlatformDao

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


        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN workoutId TEXT")
                db.execSQL("ALTER TABLE workout_sessions ADD COLUMN readinessCheckId TEXT")
                db.execSQL("ALTER TABLE set_logs ADD COLUMN exerciseId TEXT")
                db.execSQL("ALTER TABLE set_logs ADD COLUMN failed INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE assessment_results ADD COLUMN assessmentId TEXT")
                db.execSQL("ALTER TABLE assessment_results ADD COLUMN exerciseId TEXT")
                db.execSQL("ALTER TABLE skill_states ADD COLUMN athleteId TEXT NOT NULL DEFAULT 'local'")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS athlete_profiles (
                        id TEXT NOT NULL PRIMARY KEY,
                        age INTEGER,
                        heightCm REAL,
                        weightKg REAL,
                        dailyActivity TEXT NOT NULL,
                        experience TEXT NOT NULL,
                        primaryGoal TEXT NOT NULL,
                        trainingDaysPerWeek INTEGER NOT NULL,
                        sessionMinutes INTEGER NOT NULL,
                        equipmentCsv TEXT NOT NULL,
                        limitationsCsv TEXT NOT NULL,
                        targetSkillsCsv TEXT NOT NULL,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS assessments (
                        id TEXT NOT NULL PRIMARY KEY,
                        athleteId TEXT NOT NULL,
                        assessmentType TEXT NOT NULL,
                        goalContext TEXT NOT NULL,
                        status TEXT NOT NULL,
                        recordedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assessments_athleteId ON assessments(athleteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_assessments_recordedAt ON assessments(recordedAt)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS source_actions (
                        source TEXT NOT NULL,
                        sourceActionId INTEGER NOT NULL,
                        textRevision INTEGER,
                        attrsRevision INTEGER,
                        hasArabic INTEGER NOT NULL,
                        hasEnglish INTEGER NOT NULL,
                        hasEmbeddedVideo INTEGER NOT NULL,
                        mediaKey TEXT,
                        importState TEXT NOT NULL,
                        importedExerciseId TEXT,
                        PRIMARY KEY(source, sourceActionId)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_source_actions_sourceActionId ON source_actions(sourceActionId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_source_actions_hasEmbeddedVideo ON source_actions(hasEmbeddedVideo)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_source_actions_importState ON source_actions(importState)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercises (
                        id TEXT NOT NULL PRIMARY KEY,
                        technicalName TEXT NOT NULL,
                        nameEn TEXT NOT NULL,
                        nameAr TEXT NOT NULL,
                        movementPattern TEXT NOT NULL,
                        difficulty TEXT NOT NULL,
                        measurementType TEXT NOT NULL,
                        equipmentCsv TEXT NOT NULL,
                        primaryMusclesCsv TEXT NOT NULL,
                        secondaryMusclesCsv TEXT NOT NULL,
                        instructionsEn TEXT NOT NULL,
                        instructionsAr TEXT NOT NULL,
                        commonMistakesAr TEXT NOT NULL,
                        contraindicationsAr TEXT NOT NULL,
                        videoKey TEXT,
                        isCoreCatalog INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        sourceActionId INTEGER,
                        sourceRevision INTEGER,
                        updatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_exercises_technicalName ON exercises(technicalName)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_movementPattern ON exercises(movementPattern)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercises_sourceActionId ON exercises(sourceActionId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS exercise_variants (
                        exerciseId TEXT NOT NULL,
                        targetExerciseId TEXT NOT NULL,
                        relation TEXT NOT NULL,
                        difficultyDelta INTEGER NOT NULL,
                        equipmentDeltaCsv TEXT NOT NULL,
                        reasonAr TEXT NOT NULL,
                        PRIMARY KEY(exerciseId, targetExerciseId, relation),
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE,
                        FOREIGN KEY(targetExerciseId) REFERENCES exercises(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_variants_exerciseId ON exercise_variants(exerciseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_exercise_variants_targetExerciseId ON exercise_variants(targetExerciseId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS skills (
                        id TEXT NOT NULL PRIMARY KEY,
                        nameEn TEXT NOT NULL,
                        nameAr TEXT NOT NULL,
                        descriptionAr TEXT NOT NULL,
                        displayOrder INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS progression_nodes (
                        id TEXT NOT NULL PRIMARY KEY,
                        skillId TEXT NOT NULL,
                        exerciseId TEXT NOT NULL,
                        orderIndex INTEGER NOT NULL,
                        entryRuleJson TEXT NOT NULL,
                        masteryRuleJson TEXT NOT NULL,
                        minimumSessions INTEGER NOT NULL,
                        FOREIGN KEY(skillId) REFERENCES skills(id) ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progression_nodes_skillId ON progression_nodes(skillId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progression_nodes_exerciseId ON progression_nodes(exerciseId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS progression_edges (
                        fromNodeId TEXT NOT NULL,
                        toNodeId TEXT NOT NULL,
                        relation TEXT NOT NULL,
                        priority INTEGER NOT NULL,
                        PRIMARY KEY(fromNodeId, toNodeId),
                        FOREIGN KEY(fromNodeId) REFERENCES progression_nodes(id) ON DELETE CASCADE,
                        FOREIGN KEY(toNodeId) REFERENCES progression_nodes(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progression_edges_fromNodeId ON progression_edges(fromNodeId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_progression_edges_toNodeId ON progression_edges(toNodeId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS training_programs (
                        id TEXT NOT NULL PRIMARY KEY,
                        athleteId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        rationaleAr TEXT NOT NULL,
                        totalWeeks INTEGER NOT NULL,
                        daysPerWeek INTEGER NOT NULL,
                        sessionMinutes INTEGER NOT NULL,
                        status TEXT NOT NULL,
                        generatedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_training_programs_athleteId ON training_programs(athleteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_training_programs_status ON training_programs(status)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS program_weeks (
                        programId TEXT NOT NULL,
                        weekNumber INTEGER NOT NULL,
                        phaseId TEXT NOT NULL,
                        phaseTitleAr TEXT NOT NULL,
                        phaseGoalAr TEXT NOT NULL,
                        PRIMARY KEY(programId, weekNumber),
                        FOREIGN KEY(programId) REFERENCES training_programs(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_program_weeks_programId ON program_weeks(programId)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workouts (
                        id TEXT NOT NULL PRIMARY KEY,
                        programId TEXT NOT NULL,
                        weekNumber INTEGER NOT NULL,
                        dayNumber INTEGER NOT NULL,
                        titleAr TEXT NOT NULL,
                        titleEn TEXT NOT NULL,
                        estimatedMinutes INTEGER NOT NULL,
                        workoutType TEXT NOT NULL,
                        FOREIGN KEY(programId) REFERENCES training_programs(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_programId ON workouts(programId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workouts_programId_weekNumber_dayNumber ON workouts(programId, weekNumber, dayNumber)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_exercises (
                        id TEXT NOT NULL PRIMARY KEY,
                        workoutId TEXT NOT NULL,
                        exerciseId TEXT NOT NULL,
                        position INTEGER NOT NULL,
                        sets INTEGER NOT NULL,
                        targetRepsMin INTEGER,
                        targetRepsMax INTEGER,
                        targetHoldSeconds INTEGER,
                        targetLeftReps INTEGER,
                        targetRightReps INTEGER,
                        targetRpe INTEGER,
                        targetRir INTEGER,
                        restSeconds INTEGER NOT NULL,
                        assistance TEXT,
                        addedWeightKg REAL,
                        bandLevel TEXT,
                        tempo TEXT,
                        FOREIGN KEY(workoutId) REFERENCES workouts(id) ON DELETE CASCADE,
                        FOREIGN KEY(exerciseId) REFERENCES exercises(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_workoutId ON workout_exercises(workoutId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_workout_exercises_exerciseId ON workout_exercises(exerciseId)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_workout_exercises_workoutId_position ON workout_exercises(workoutId, position)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS readiness_checks (
                        id TEXT NOT NULL PRIMARY KEY,
                        athleteId TEXT NOT NULL,
                        energy TEXT NOT NULL,
                        timeAvailableMinutes INTEGER NOT NULL,
                        equipmentAvailableCsv TEXT NOT NULL,
                        soreness TEXT NOT NULL,
                        notes TEXT NOT NULL,
                        recordedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_readiness_checks_athleteId ON readiness_checks(athleteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_readiness_checks_recordedAt ON readiness_checks(recordedAt)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS performance_history (
                        id TEXT NOT NULL PRIMARY KEY,
                        athleteId TEXT NOT NULL,
                        exerciseId TEXT NOT NULL,
                        sessionId TEXT NOT NULL,
                        successfulSets INTEGER NOT NULL,
                        attemptedSets INTEGER NOT NULL,
                        bestReps INTEGER,
                        bestHoldSeconds INTEGER,
                        totalReps INTEGER,
                        averageRpe REAL,
                        averageRir REAL,
                        recordedAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_performance_history_athleteId ON performance_history(athleteId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_performance_history_exerciseId ON performance_history(exerciseId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_performance_history_recordedAt ON performance_history(recordedAt)")

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS personal_bests (
                        athleteId TEXT NOT NULL,
                        exerciseId TEXT NOT NULL,
                        metric TEXT NOT NULL,
                        value REAL NOT NULL,
                        unit TEXT NOT NULL,
                        sessionId TEXT,
                        achievedAt INTEGER NOT NULL,
                        PRIMARY KEY(athleteId, exerciseId, metric)
                    )
                """.trimIndent())
                db.execSQL("CREATE INDEX IF NOT EXISTS index_personal_bests_exerciseId ON personal_bests(exerciseId)")
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "calisthenics.db"
            )
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
                .also { instance = it }
        }
    }
}

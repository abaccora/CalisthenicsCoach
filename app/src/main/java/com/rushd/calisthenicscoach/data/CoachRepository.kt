package com.rushd.calisthenicscoach.data

import kotlinx.coroutines.flow.Flow

class CoachRepository(private val db: AppDatabase) {
    private val dao = db.coachDao()

    val assessments: Flow<List<AssessmentResultEntity>> = dao.observeAssessments()
    val skillStates: Flow<List<SkillStateEntity>> = dao.observeSkillStates()
    val exerciseHistory: Flow<List<ExerciseHistoryStatRow>> = dao.observeExerciseHistory()

    suspend fun saveAssessment(results: List<AssessmentResultEntity>) {
        if (results.isNotEmpty()) dao.insertAssessmentResults(results)
    }

    suspend fun saveSkillStates(states: List<SkillStateEntity>) {
        states.forEach { dao.upsertSkillState(it) }
    }
}

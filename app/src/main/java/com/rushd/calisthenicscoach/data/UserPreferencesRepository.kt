package com.rushd.calisthenicscoach.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.rushd.calisthenicscoach.domain.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.userProfileStore by preferencesDataStore(name = "user_profile_v2")

class UserPreferencesRepository(private val context: Context) {

    private object Keys {
        val completed = booleanPreferencesKey("completed")
        val goal = stringPreferencesKey("goal")
        val experience = stringPreferencesKey("experience")
        val days = intPreferencesKey("days")
        val minutes = intPreferencesKey("minutes")
        val equipment = stringSetPreferencesKey("equipment")
        val skills = stringSetPreferencesKey("skills")
        val pushUps = intPreferencesKey("push_ups")
        val pullUps = intPreferencesKey("pull_ups")
        val dips = intPreferencesKey("dips")
        val hollow = intPreferencesKey("hollow")
    }

    val profile: Flow<UserProfile?> = context.userProfileStore.data.map { p ->
        if (p[Keys.completed] != true) null else UserProfile(
            goal = p[Keys.goal] ?: "قوة عامة",
            experience = p[Keys.experience] ?: "مبتدئ",
            daysPerWeek = p[Keys.days] ?: 3,
            sessionMinutes = p[Keys.minutes] ?: 45,
            equipment = p[Keys.equipment] ?: emptySet(),
            targetSkills = p[Keys.skills] ?: emptySet(),
            maxPushUps = p[Keys.pushUps] ?: 0,
            maxPullUps = p[Keys.pullUps] ?: 0,
            maxDips = p[Keys.dips] ?: 0,
            hollowHoldSec = p[Keys.hollow] ?: 0,
            onboardingCompleted = true
        )
    }

    suspend fun save(profile: UserProfile) {
        context.userProfileStore.edit { p ->
            p[Keys.completed] = true
            p[Keys.goal] = profile.goal
            p[Keys.experience] = profile.experience
            p[Keys.days] = profile.daysPerWeek
            p[Keys.minutes] = profile.sessionMinutes
            p[Keys.equipment] = profile.equipment
            p[Keys.skills] = profile.targetSkills
            p[Keys.pushUps] = profile.maxPushUps
            p[Keys.pullUps] = profile.maxPullUps
            p[Keys.dips] = profile.maxDips
            p[Keys.hollow] = profile.hollowHoldSec
        }
    }

    suspend fun reset() {
        context.userProfileStore.edit { it.clear() }
    }
}

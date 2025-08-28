package com.enlightenment.ai.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.enlightenment.ai.domain.model.ChildProfile
import com.enlightenment.ai.domain.model.LearningLevel
import com.enlightenment.ai.domain.model.LearningStyle
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val gson: Gson
) : ProfileRepository {
    
    companion object {
        private val PROFILE_KEY = stringPreferencesKey("child_profile")
    }
    
    override suspend fun getProfile(): ChildProfile? {
        return try {
            val preferences = dataStore.data.catch { 
                if (it is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }.map { preferences ->
                preferences[PROFILE_KEY]?.let { json ->
                    gson.fromJson(json, ChildProfile::class.java)
                }
            }.collect { profile ->
                return@collect profile
            }
            null
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun saveProfile(profile: ChildProfile) {
        dataStore.edit { preferences ->
            preferences[PROFILE_KEY] = gson.toJson(profile)
        }
    }
    
    override fun observeProfile(): Flow<ChildProfile?> {
        return dataStore.data
            .catch { 
                if (it is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw it
                }
            }
            .map { preferences ->
                preferences[PROFILE_KEY]?.let { json ->
                    gson.fromJson(json, ChildProfile::class.java)
                }
            }
    }
    
    override suspend fun updateInterests(interests: List<String>) {
        val currentProfile = getProfile() ?: return
        saveProfile(currentProfile.copy(interests = interests))
    }
    
    override suspend fun updateLearningProgress(topic: String, score: Float) {
        val currentProfile = getProfile() ?: return
        val updatedTopics = currentProfile.recentTopics.toMutableList().apply {
            add(0, topic)
            if (size > 10) removeAt(size - 1)
        }
        
        // Update learning level based on score
        val newLevel = when {
            score > 0.9f && currentProfile.learningLevel == LearningLevel.NORMAL -> LearningLevel.ADVANCED
            score < 0.5f && currentProfile.learningLevel == LearningLevel.NORMAL -> LearningLevel.BEGINNER
            else -> currentProfile.learningLevel
        }
        
        saveProfile(currentProfile.copy(
            recentTopics = updatedTopics,
            learningLevel = newLevel
        ))
    }
}
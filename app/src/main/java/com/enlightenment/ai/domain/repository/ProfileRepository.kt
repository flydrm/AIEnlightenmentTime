package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.ChildProfile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    suspend fun getProfile(): ChildProfile?
    
    suspend fun saveProfile(profile: ChildProfile)
    
    fun observeProfile(): Flow<ChildProfile?>
    
    suspend fun updateInterests(interests: List<String>)
    
    suspend fun updateLearningProgress(topic: String, score: Float)
}
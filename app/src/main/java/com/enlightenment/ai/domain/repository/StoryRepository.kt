package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun generateStory(
        childAge: Int,
        interests: List<String>,
        theme: String? = null
    ): Result<Story>
    
    suspend fun getStoryById(id: String): Story?
    
    suspend fun saveStory(story: Story)
    
    fun getRecentStories(): Flow<List<Story>>
    
    suspend fun getCachedStories(): List<Story>
}
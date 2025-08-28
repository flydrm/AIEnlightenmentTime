package com.enlightenment.ai.data.repository

import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.entity.StoryEntity
import com.enlightenment.ai.data.remote.api.AIApiService
import com.enlightenment.ai.data.remote.model.QuestionResponse
import com.enlightenment.ai.data.remote.model.StoryResponse
import com.enlightenment.ai.domain.model.Story
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class StoryRepositoryImplTest {
    
    private lateinit var apiService: AIApiService
    private lateinit var storyDao: StoryDao
    private lateinit var gson: Gson
    private lateinit var repository: StoryRepositoryImpl
    
    @Before
    fun setup() {
        apiService = mockk()
        storyDao = mockk(relaxed = true)
        gson = Gson()
        repository = StoryRepositoryImpl(apiService, storyDao, gson)
    }
    
    @Test
    fun `generateStory should return success when API call succeeds`() = runTest {
        // Given
        val apiResponse = StoryResponse(
            id = "story-1",
            title = "Test Story",
            content = "Once upon a time...",
            imageUrl = "https://example.com/image.jpg",
            duration = 180,
            questions = listOf(
                QuestionResponse(
                    id = "q1",
                    text = "What color is the panda?",
                    options = listOf("Red", "Black", "White"),
                    correctAnswerIndex = 0,
                    explanation = "Red pandas are red!"
                )
            ),
            metadata = null
        )
        
        coEvery { apiService.generateStory(any()) } returns apiResponse
        
        // When
        val result = repository.generateStory(
            childAge = 5,
            interests = listOf("animals"),
            theme = "friendship"
        )
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val story = result.getOrNull()!!
        assertThat(story.id).isEqualTo("story-1")
        assertThat(story.title).isEqualTo("Test Story")
        assertThat(story.questions).hasSize(1)
        
        // Verify story was saved
        coVerify { storyDao.insertStory(any()) }
    }
    
    @Test
    fun `generateStory should return cached story when API fails`() = runTest {
        // Given
        val cachedEntity = StoryEntity(
            id = "cached-1",
            title = "Cached Story",
            content = "Cached content",
            imageUrl = null,
            duration = 120,
            questions = "[]",
            createdAt = System.currentTimeMillis(),
            childAge = 4
        )
        
        coEvery { apiService.generateStory(any()) } throws Exception("Network error")
        coEvery { storyDao.getAllStories() } returns listOf(cachedEntity)
        
        // When
        val result = repository.generateStory(
            childAge = 5,
            interests = emptyList(),
            theme = null
        )
        
        // Then
        assertThat(result.isSuccess).isTrue()
        val story = result.getOrNull()!!
        assertThat(story.title).isEqualTo("Cached Story")
    }
    
    @Test
    fun `getRecentStories should return flow of stories`() = runTest {
        // Given
        val entities = listOf(
            StoryEntity(
                id = "1",
                title = "Story 1",
                content = "Content 1",
                imageUrl = null,
                duration = 120,
                questions = "[]",
                createdAt = System.currentTimeMillis(),
                childAge = 4
            )
        )
        
        coEvery { storyDao.getRecentStories(any()) } returns flowOf(entities)
        
        // When
        repository.getRecentStories().collect { stories ->
            // Then
            assertThat(stories).hasSize(1)
            assertThat(stories.first().title).isEqualTo("Story 1")
        }
    }
}
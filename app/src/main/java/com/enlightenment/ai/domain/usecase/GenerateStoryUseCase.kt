package com.enlightenment.ai.domain.usecase

import com.enlightenment.ai.domain.model.Story
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.enlightenment.ai.domain.repository.StoryRepository
import javax.inject.Inject

class GenerateStoryUseCase @Inject constructor(
    private val storyRepository: StoryRepository,
    private val profileRepository: ProfileRepository
) {
    suspend operator fun invoke(theme: String? = null): Result<Story> {
        val profile = profileRepository.getProfile()
        
        return storyRepository.generateStory(
            childAge = profile?.age ?: 4,
            interests = profile?.interests ?: emptyList(),
            theme = theme
        )
    }
}
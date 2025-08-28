package com.enlightenment.ai.di

import com.enlightenment.ai.data.repository.DialogueRepositoryImpl
import com.enlightenment.ai.data.repository.ProfileRepositoryImpl
import com.enlightenment.ai.data.repository.StoryRepositoryImpl
import com.enlightenment.ai.data.repository.LearningStatsRepositoryImpl
import com.enlightenment.ai.domain.repository.DialogueRepository
import com.enlightenment.ai.domain.repository.ProfileRepository
import com.enlightenment.ai.domain.repository.StoryRepository
import com.enlightenment.ai.domain.repository.LearningStatsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindStoryRepository(
        storyRepositoryImpl: StoryRepositoryImpl
    ): StoryRepository
    
    @Binds
    @Singleton
    abstract fun bindDialogueRepository(
        dialogueRepositoryImpl: DialogueRepositoryImpl
    ): DialogueRepository
    
    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        profileRepositoryImpl: ProfileRepositoryImpl
    ): ProfileRepository
    
    @Binds
    @Singleton
    abstract fun bindLearningStatsRepository(
        learningStatsRepositoryImpl: LearningStatsRepositoryImpl
    ): LearningStatsRepository
}
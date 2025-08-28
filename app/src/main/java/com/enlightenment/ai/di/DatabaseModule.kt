package com.enlightenment.ai.di

import android.content.Context
import androidx.room.Room
import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "enlightenment.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }
}
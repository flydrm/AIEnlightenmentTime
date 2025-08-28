package com.enlightenment.ai.data.local.dao

import androidx.room.*
import com.enlightenment.ai.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoryDao {
    @Query("SELECT * FROM stories ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentStories(limit: Int = 10): Flow<List<StoryEntity>>
    
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")
    suspend fun getAllStories(): List<StoryEntity>
    
    @Query("SELECT * FROM stories WHERE id = :id")
    suspend fun getStoryById(id: String): StoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    @Update
    suspend fun updateStory(story: StoryEntity)
    
    @Query("UPDATE stories SET lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE id = :id")
    suspend fun updatePlayInfo(id: String, timestamp: Long)
    
    @Query("DELETE FROM stories WHERE createdAt < :threshold")
    suspend fun deleteOldStories(threshold: Long)
    
    @Query("DELETE FROM stories")
    suspend fun deleteAllStories()
}
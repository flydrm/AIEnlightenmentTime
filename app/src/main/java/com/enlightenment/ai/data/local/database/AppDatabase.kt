package com.enlightenment.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.dao.DialogueMessageDao
import com.enlightenment.ai.data.local.entity.Converters
import com.enlightenment.ai.data.local.entity.StoryEntity
import com.enlightenment.ai.data.local.entity.DialogueMessageEntity

@Database(
    entities = [StoryEntity::class, DialogueMessageEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun dialogueMessageDao(): DialogueMessageDao
}
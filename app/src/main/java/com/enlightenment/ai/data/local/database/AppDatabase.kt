package com.enlightenment.ai.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.entity.Converters
import com.enlightenment.ai.data.local.entity.StoryEntity

@Database(
    entities = [StoryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
}
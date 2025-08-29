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
abstract /**
 * 数据层 - 本地存储 - AppDatabase
 * 
 * 功能说明：
 * 提供特定功能的实现。
 * 
 * 技术特点：
 * - 遵循Clean Architecture原则
 * - 支持协程异步操作
 * - 依赖注入管理
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */
/**
 * AppDatabase - AppDatabase
 * 
 * Room数据库配置类，管理应用的本地数据存储
 * 
 * 数据库特性：
 * - 自动模式迁移
 * - 类型转换器支持
 * - 多线程安全访问
 * 
 * @since 1.0.0
 */
class AppDatabase : RoomDatabase() {
    abstract fun storyDao(): StoryDao
    abstract fun dialogueMessageDao(): DialogueMessageDao
}
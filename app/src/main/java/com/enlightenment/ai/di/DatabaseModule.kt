package com.enlightenment.ai.di

import android.content.Context
import androidx.room.Room
import com.enlightenment.ai.data.local.dao.StoryDao
import com.enlightenment.ai.data.local.dao.DialogueMessageDao
import com.enlightenment.ai.data.local.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 依赖注入模块 - 数据库配置
 * 
 * 职责说明：
 * 配置和提供Room数据库相关的依赖实例。
 * 确保数据库在应用生命周期内单例存在。
 * 
 * 提供的依赖：
 * 1. AppDatabase - 应用主数据库实例
 * 2. StoryDao - 故事数据访问对象
 * 3. DialogueMessageDao - 对话消息数据访问对象
 * 
 * 配置特点：
 * - 单例模式：整个应用共享一个数据库实例
 * - 破坏性迁移：版本升级时清空数据（开发阶段）
 * - 自动依赖：Hilt自动管理生命周期
 * 
 * 数据库策略：
 * - 数据库名：enlightenment.db
 * - 存储位置：应用私有目录
 * - 迁移策略：生产环境需改为Migration
 * 
 * @Module Hilt模块标注
 * @InstallIn 安装到SingletonComponent，全局单例
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * DatabaseModule - Database模块
 * 
 * Hilt依赖注入模块，提供依赖的创建和配置
 * 
 * 模块职责：
 * - 定义依赖的生命周期
 * - 配置依赖的创建方式
 * - 管理依赖的作用域
 * 
 * 依赖管理：
 * - 单例模式的全局依赖
 * - 作用域限定的依赖
 * - 限定符区分的依赖
 * 
 * @since 1.0.0
 */
object DatabaseModule {
    
    /**
     * 提供应用数据库实例
     * 
     * 配置说明：
     * 1. 使用Room.databaseBuilder创建数据库
     * 2. 数据库文件名：enlightenment.db
     * 3. 破坏性迁移：版本变更时清空数据
     * 
     * 注意事项：
     * - 生产环境必须实现Migration避免数据丢失
     * - 数据库操作默认在后台线程执行
     * - 支持WAL模式提升并发性能
     * 
     * @param context 应用上下文
     * @return 数据库单例实例
     */
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
            .fallbackToDestructiveMigration()  // 开发阶段使用，生产需替换
            .build()
    }
    
    /**
     * 提供故事数据访问对象
     * 
     * @param 数据库 应用数据库实例
     * @return StoryDao实例
     */
    @Provides
    fun provideStoryDao(database: AppDatabase): StoryDao {
        return database.storyDao()
    }
    
    /**
     * 提供对话消息数据访问对象
     * 
     * @param 数据库 应用数据库实例
     * @return DialogueMessageDao实例
     */
    @Provides
    fun provideDialogueMessageDao(database: AppDatabase): DialogueMessageDao {
        return database.dialogueMessageDao()
    }
}
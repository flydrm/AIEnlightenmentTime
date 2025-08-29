package com.enlightenment.ai.data.local.dao

import androidx.room.*
import com.enlightenment.ai.data.local.entity.StoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * 数据访问对象 - 故事表
 * 
 * 职责说明：
 * 定义故事数据的本地数据库操作接口。
 * 使用Room框架自动生成SQL语句，提供类型安全的数据访问。
 * 
 * 核心功能：
 * 1. 故事的增删改查操作
 * 2. 响应式数据查询（Flow）
 * 3. 播放记录更新
 * 4. 历史数据清理
 * 
 * 数据管理策略：
 * - 缓存策略：保留最近的故事供离线使用
 * - 清理策略：定期删除过期故事释放空间
 * - 更新策略：冲突时替换旧数据
 * 
 * 性能优化：
 * - 使用Flow实现响应式查询
 * - 限制查询数量避免内存溢出
 * - 索引优化提升查询速度
 * 
 * @Dao Room数据访问对象标注
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Dao
/**
 * StoryDao - StoryDao
 * 
 * Room数据访问对象，提供类型安全的数据库操作接口
 * 
 * 支持的操作：
 * - 增删改查基础操作
 * - 复杂查询和关联查询
 * - 事务支持和批量操作
 * 
 * @since 1.0.0
 */
interface StoryDao {
    /**
     * 获取最近的故事列表
     * 
     * 返回最新创建的故事，用于首页展示和离线访问。
     * 使用Flow实现响应式更新，数据变化时自动通知UI。
     * 
     * @param limit 返回数量限制，默认10条
     * @return 故事列表的响应式数据流
     */
    @Query("SELECT * FROM stories ORDER BY createdAt DESC LIMIT :limit")  // 查询数据库
    fun getRecentStories(limit: Int = 10): Flow<List<StoryEntity>>
    
    /**
     * 获取所有故事
     * 
     * 一次性获取所有缓存的故事，用于数据导出或统计。
     * 注意：数据量大时可能影响性能。
     * 
     * @return 所有故事列表，按创建时间倒序
     */
    @Query("SELECT * FROM stories ORDER BY createdAt DESC")  // 查询数据库
    suspend fun getAllStories(): List<StoryEntity>
    
    /**
     * 根据ID获取故事
     * 
     * 用于查看故事详情或继续播放。
     * 
     * @param id 故事唯一标识
     * @return 故事实体，不存在返回null
     */
    @Query("SELECT * FROM stories WHERE id = :id")  // 查询数据库
    suspend fun getStoryById(id: String): StoryEntity?
    
    /**
     * 插入或更新故事
     * 
     * 保存新生成的故事或更新已存在的故事。
     * 使用REPLACE策略，ID相同时替换旧数据。
     * 
     * @param story 要保存的故事实体
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStory(story: StoryEntity)
    
    /**
     * 更新故事信息
     * 
     * 更新故事的非主键字段，如标题、内容等。
     * 
     * @param story 包含更新信息的故事实体
     */
    @Update
    suspend fun updateStory(story: StoryEntity)
    
    /**
     * 更新播放信息
     * 
     * 记录故事的播放时间和次数，用于推荐算法和统计。
     * 每次播放时调用，自动增加播放计数。
     * 
     * @param id 故事ID
     * @param timestamp 播放时间戳
     */
    @Query("UPDATE stories SET lastPlayedAt = :timestamp, playCount = playCount + 1 WHERE id = :id")  // 更新数据
    suspend fun updatePlayInfo(id: String, timestamp: Long)
    
    /**
     * 删除过期故事
     * 
     * 清理指定时间之前的故事，释放存储空间。
     * 建议保留最近30天的数据。
     * 
     * @param threshold 时间阈值，删除此时间之前创建的故事
     */
    @Query("DELETE FROM stories WHERE createdAt < :threshold")  // 删除数据
    suspend fun deleteOldStories(threshold: Long)
    
    /**
     * 清空所有故事
     * 
     * 危险操作：删除所有缓存的故事数据。
     * 仅在用户主动清理或重置应用时使用。
     */
    @Query("DELETE FROM stories")  // 删除数据
    suspend fun deleteAllStories()
}
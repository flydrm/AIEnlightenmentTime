package com.enlightenment.ai.data.local.offline

import android.content.Context
import com.enlightenment.ai.domain.model.Story
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 离线内容管理器
 * 负责管理和提供离线内容
 */
@Singleton
/**
 * OfflineContentManager - 离线内容管理器
 * 
 * 本地数据存储组件，提供离线数据支持
 * 
 * @自版本 1.0.0
 */
class OfflineContentManager @Inject constructor(  // 依赖注入
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    
    private val offlineDir = File(context.filesDir, "offline_content")
    private val storiesDir = File(offlineDir, "stories")
    private val imagesDir = File(offlineDir, "images")
    
    init {
        // 确保目录存在
        storiesDir.mkdirs()
        imagesDir.mkdirs()
        
        // 预加载默认离线内容
        preloadDefaultContent()
    }
    
    /**
     * 保存故事到离线存储
     */
    suspend fun saveStoryOffline(story: Story) = withContext(Dispatchers.IO) {
        try {
            val storyFile = File(storiesDir, "${story.id}.json")
            val json = gson.toJson(story)
            storyFile.writeText(json)
            
            // 如果有图片，也保存图片
            story.imageUrl?.let { url ->
                // 这里应该下载并保存图片
                // 简化实现，实际应该使用图片下载库
            }
        } catch (e: Exception) {  // 捕获并处理异常
            // 记录错误但不抛出，离线保存失败不应影响正常流程
        }
    }
    
    /**
     * 获取离线故事列表
     */
    suspend fun getOfflineStories(): List<Story> = withContext(Dispatchers.IO) {
        try {
            storiesDir.listFiles()
                ?.filter { it.extension == "json" }
                ?.mapNotNull { file ->
                    try {
                        val json = file.readText()
                        gson.fromJson(json, Story::class.java)
                    } catch (e: Exception) {  // 捕获并处理异常
                        null
                    }
                } ?: emptyList()
        } catch (e: Exception) {  // 捕获并处理异常
            emptyList()
        }
    }
    
    /**
     * 获取随机离线故事
     */
    suspend fun getRandomOfflineStory(): Story? = withContext(Dispatchers.IO) {
        getOfflineStories().randomOrNull()
    }
    
    /**
     * 预加载默认内容
     */
    private fun preloadDefaultContent() {
        // 预置一些默认故事，确保完全离线也能使用
        val defaultStories = listOf(
            Story(
                id = "offline_1",
                title = "小红帽的故事",
                content = """
                    从前，有一个可爱的小女孩，她总是戴着奶奶送的红色帽子，所以大家都叫她小红帽。
                    
                    有一天，妈妈让小红帽去看望生病的奶奶。妈妈准备了一篮子好吃的，并嘱咐小红帽："要走大路，不要和陌生人说话哦！"
                    
                    小红帽开心地出发了。在森林里，她遇到了一只大灰狼。大灰狼假装很友善地问："小朋友，你要去哪里呀？"
                    
                    小红帽记起了妈妈的话，说："我不能告诉陌生人。"然后快步向前走。
                    
                    大灰狼很生气，但它知道奶奶家在哪里，于是抄近路先到了奶奶家...
                    
                    最后，勇敢的猎人救了小红帽和奶奶，大家都安全了！
                    
                    这个故事告诉我们：要听爸爸妈妈的话，不要随便相信陌生人。
                """.trimIndent(),
                imageUrl = null,
                duration = 5,
                questions = listOf(),
                childAge = 4
            ),
            Story(
                id = "offline_2",
                title = "三只小猪",
                content = """
                    从前，有三只小猪，他们长大了要盖自己的房子。
                    
                    第一只小猪很懒，用稻草盖了一间房子，很快就盖好了。
                    
                    第二只小猪用木头盖房子，比稻草结实一些，但也很快盖好了。
                    
                    第三只小猪很勤劳，他用砖头一块一块地盖房子，虽然很累，但房子非常结实。
                    
                    有一天，大灰狼来了！它轻轻一吹，就把稻草房子吹倒了。第一只小猪赶紧跑到第二只小猪家。
                    
                    大灰狼又来吹木头房子，也吹倒了！两只小猪赶紧跑到第三只小猪的砖头房子里。
                    
                    大灰狼使劲吹砖头房子，可是怎么也吹不倒。最后，大灰狼只好灰溜溜地走了。
                    
                    这个故事告诉我们：做事情要认真，不要偷懒哦！
                """.trimIndent(),
                imageUrl = null,
                duration = 5,
                questions = listOf(),
                childAge = 4
            )
        )
        
        // 保存默认故事
        defaultStories.forEach { story ->
            val storyFile = File(storiesDir, "${story.id}.json")
            if (!storyFile.exists()) {
                try {
                    val json = gson.toJson(story)
                    storyFile.writeText(json)
                } catch (e: Exception) {  // 捕获并处理异常
                    // 忽略错误
                }
            }
        }
    }
    
    /**
     * 清理过期的离线内容
     */
    suspend fun cleanupOldContent(daysToKeep: Int = 30) = withContext(Dispatchers.IO) {
        val cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
        
        storiesDir.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoffTime) {
                file.delete()
            }
        }
    }
    
    /**
     * 获取离线内容大小
     */
    fun getOfflineContentSize(): Long {
        return offlineDir.walkTopDown().filter { it.isFile }.map { it.length() }.sum()
    }
}
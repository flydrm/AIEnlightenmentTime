package com.enlightenment.ai.presentation.common

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 文字转语音管理器
 * 
 * 功能说明：
 * 封装Android TTS（Text-to-Speech）功能，为应用提供语音朗读能力。
 * 专门针对儿童用户优化了语速和音调，使语音更加亲切自然。
 * 
 * 核心功能：
 * 1. 故事内容朗读
 * 2. 界面元素语音提示
 * 3. 问题和答案播报
 * 4. 操作引导语音
 * 
 * 技术特点：
 * - 单例模式确保全局唯一实例
 * - StateFlow提供响应式状态
 * - 自动管理TTS生命周期
 * - 支持中文语音合成
 * 
 * 优化策略：
 * - 语速：0.9倍速，让儿童更容易理解
 * - 音调：1.1倍调，声音更加活泼亲切
 * - 队列：QUEUE_FLUSH模式，避免语音重叠
 * 
 * @property context 应用上下文，用于初始化TTS引擎
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Singleton
class TextToSpeechManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private var tts: TextToSpeech? = null
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()
    
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()
    
    init {
        initializeTTS()
    }
    
    /**
     * 初始化TTS引擎
     * 
     * 初始化流程：
     * 1. 创建TTS实例
     * 2. 设置简体中文语言
     * 3. 调整语音参数
     * 4. 配置进度监听器
     * 
     * 参数优化说明：
     * - 语速0.9：比正常稍慢，便于儿童理解
     * - 音调1.1：比正常稍高，声音更活泼
     * - 中文优先：默认使用简体中文
     * 
     * 错误处理：
     * - 语言包缺失：保持未初始化状态
     * - 引擎创建失败：静默失败，避免崩溃
     */
    private fun initializeTTS() {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.let { engine ->
                    // 设置中文语言
                    val result = engine.setLanguage(Locale.SIMPLIFIED_CHINESE)
                    if (result != TextToSpeech.LANG_MISSING_DATA && 
                        result != TextToSpeech.LANG_NOT_SUPPORTED) {
                        _isInitialized.value = true
                        
                        // 设置语音参数
                        engine.setSpeechRate(0.9f) // 稍慢的语速，适合儿童
                        engine.setPitch(1.1f) // 稍高的音调，更亲切
                        
                        // 设置进度监听
                        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                _isSpeaking.value = true
                            }
                            
                            override fun onDone(utteranceId: String?) {
                                _isSpeaking.value = false
                            }
                            
                            override fun onError(utteranceId: String?) {
                                _isSpeaking.value = false
                            }
                        })
                    }
                }
            }
        }
    }
    
    /**
     * 朗读文本
     * 
     * 功能说明：
     * 将文字转换为语音并播放。
     * 使用QUEUE_FLUSH模式，新的朗读会中断当前朗读。
     * 
     * @param text 要朗读的文本内容
     * @param utteranceId 朗读任务的唯一标识，用于跟踪进度
     * 
     * 使用场景：
     * - 朗读故事内容
     * - 播报问题选项
     * - 语音引导操作
     */
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (_isInitialized.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }
    
    /**
     * 停止朗读
     * 
     * 立即停止当前的语音播放，清空待播放队列。
     * 同时更新播放状态为false。
     */
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }
    
    /**
     * 暂停朗读
     * 
     * 如果正在朗读，则停止播放。
     * 注意：Android TTS不支持真正的暂停，只能停止。
     * 如需继续，需要重新调用speak方法。
     */
    fun pause() {
        if (_isSpeaking.value) {
            stop()
        }
    }
    
    /**
     * 恢复朗读
     * 
     * 重新开始朗读指定文本。
     * 由于TTS不支持从暂停位置继续，会从头开始朗读。
     * 
     * @param text 要朗读的完整文本
     * @param utteranceId 朗读任务标识
     */
    fun resume(text: String, utteranceId: String) {
        speak(text, utteranceId)
    }
    
    /**
     * 释放资源
     * 
     * 清理流程：
     * 1. 停止当前朗读
     * 2. 关闭TTS引擎
     * 3. 释放内存引用
     * 4. 重置所有状态
     * 
     * 调用时机：
     * - Activity销毁时
     * - 应用退出时
     * - 不再需要语音功能时
     */
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
        _isSpeaking.value = false
    }
}
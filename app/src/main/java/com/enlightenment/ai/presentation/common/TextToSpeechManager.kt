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
    
    fun speak(text: String, utteranceId: String = UUID.randomUUID().toString()) {
        if (_isInitialized.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
        }
    }
    
    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }
    
    fun pause() {
        if (_isSpeaking.value) {
            stop()
        }
    }
    
    fun resume(text: String, utteranceId: String) {
        speak(text, utteranceId)
    }
    
    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isInitialized.value = false
        _isSpeaking.value = false
    }
}
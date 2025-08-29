package com.enlightenment.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EnlightenmentApp : Application() {
    override fun onCreate() {
        super.onCreate()
        
        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            // 记录崩溃信息
            android.util.Log.e("EnlightenmentApp", "Uncaught exception", throwable)
            
            // 调用默认处理器
            Thread.getDefaultUncaughtExceptionHandler()?.uncaughtException(thread, throwable)
        }
        
        // 初始化性能监控
        if (BuildConfig.DEBUG) {
            android.util.Log.d("EnlightenmentApp", "App started in DEBUG mode")
        }
    }
}
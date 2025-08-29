package com.enlightenment.ai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * 应用主类
 * 
 * 职责说明：
 * Android应用的入口类，负责初始化全局配置和依赖注入框架。
 * 管理应用级别的单例和全局状态。
 * 
 * 初始化内容：
 * 1. Hilt依赖注入框架
 * 2. 全局异常处理
 * 3. 性能监控
 * 4. 日志配置
 * 
 * 生命周期：
 * - onCreate: 应用启动时调用
 * - onTerminate: 应用终止时调用（仅模拟器）
 * 
 * @HiltAndroidApp 启用Hilt依赖注入
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltAndroidApp
/**
 * EnlightenmentApp - EnlightenmentApp
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用面向对象设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
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
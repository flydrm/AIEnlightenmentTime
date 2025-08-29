package com.enlightenment.ai.di

import android.content.Context
import com.enlightenment.ai.core.analytics.AnalyticsManager
import com.enlightenment.ai.core.performance.PerformanceMonitor
import com.enlightenment.ai.core.security.SecurityManager
import com.enlightenment.ai.data.local.offline.OfflineContentManager
import com.enlightenment.ai.data.remote.NetworkRetryPolicy
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 依赖注入模块 - Core配置
 * 
 * 职责说明：
 * 配置和提供Core相关的依赖实例。
 * 确保依赖在适当的作用域内正确创建和管理。
 * 
 * 提供的依赖：
 * 根据模块功能动态调整
 * 
 * 配置特点：
 * - 生命周期管理
 * - 单例或作用域实例
 * - 自动依赖注入
 * 
 * @Module Hilt模块标注
 * @InstallIn 指定安装组件
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
/**
 * CoreModule - Core模块
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
object CoreModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson = Gson()
    
    @Provides
    @Singleton
    fun provideNetworkRetryPolicy(): NetworkRetryPolicy = NetworkRetryPolicy()
    
    @Provides
    @Singleton
    fun provideSecurityManager(
        @ApplicationContext context: Context
    ): SecurityManager = SecurityManager(context)
    
    @Provides
    @Singleton
    fun provideOfflineContentManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): OfflineContentManager = OfflineContentManager(context, gson)
    
    @Provides
    @Singleton
    fun provideAnalyticsManager(
        @ApplicationContext context: Context
    ): AnalyticsManager = AnalyticsManager(context)
    
    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor = PerformanceMonitor()
}
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

@Module
@InstallIn(SingletonComponent::class)
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
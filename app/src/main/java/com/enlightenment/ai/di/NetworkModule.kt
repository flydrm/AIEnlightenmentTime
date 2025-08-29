package com.enlightenment.ai.di

import com.enlightenment.ai.BuildConfig
import com.enlightenment.ai.data.remote.api.AIApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 依赖注入模块 - Network配置
 * 
 * 职责说明：
 * 配置和提供Network相关的依赖实例。
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
 * NetworkModule - Network模块
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
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }
    
    @Provides
    @Singleton
    /**
         * provideOkHttpClient - provideOkHttpClient方法
         * 
         * 功能描述：
         * - 执行相关相关操作
         * - 包含复杂的业务逻辑处理
         * - 确保操作的原子性和一致性
         * 
         * 实现复杂度：
         * - 方法行数: 22行
         * - 控制流: 1个
         * 
         * 注意事项：
         * - 此方法包含复杂逻辑，修改时请谨慎
         * - 确保所有分支都有正确的错误处理
         * - 保持代码的可读性和可维护性
         */
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .build()
                chain.proceed(request)
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAIApiService(retrofit: Retrofit): AIApiService {
        return retrofit.create(AIApiService::class.java)
    }
}
package com.enlightenment.ai.domain.repository

import com.enlightenment.ai.domain.usecase.RecognitionResult
import java.io.File

/**
 * 领域层 - ImageRecognitionRepository接口
 * 
 * 职责说明：
 * 定义ImageRecognition相关的数据操作契约。
 * 遵循依赖倒置原则，让数据层依赖于领域层的抽象。
 * 
 * 核心功能：
 * 1. 定义数据操作方法
 * 2. 规范返回类型
 * 3. 约定异常处理
 * 
 * 设计原则：
 * - 接口隔离：只定义必要的方法
 * - 技术无关：不涉及具体实现
 * - 单一职责：专注于特定领域
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
/**
 * ImageRecognitionRepository - ImageRecognition仓库
 * 
 * 仓库接口定义，抽象数据访问操作
 * 
 * 接口设计：
 * - 定义数据访问契约
 * - 隐藏数据源实现细节
 * - 支持依赖注入
 * 
 * 实现要求：
 * - 线程安全
 * - 错误处理
 * - 资源管理
 * 
 * @自版本 1.0.0
 */
interface ImageRecognitionRepository {
    suspend fun recognizeImage(imageFile: File): Result<RecognitionResult>
    suspend fun getCachedRecognitions(): List<RecognitionResult>
}
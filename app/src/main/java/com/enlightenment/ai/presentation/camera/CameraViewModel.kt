package com.enlightenment.ai.presentation.camera

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.enlightenment.ai.domain.usecase.RecognizeImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

/**
 * 相机功能ViewModel
 * 
 * 职责说明：
 * 管理拍照识别功能的UI状态和业务逻辑。
 * 通过AI图像识别，帮助儿童认识周围的事物。
 * 
 * 核心功能：
 * 1. 图像拍摄状态管理
 * 2. AI图像识别调用
 * 3. 识别结果展示
 * 4. 错误降级处理
 * 
 * 教育理念：
 * - 探索式学习：鼓励儿童探索周围环境
 * - 即时反馈：快速识别并提供教育信息
 * - 趣味知识：每个识别结果都包含有趣的知识点
 * 
 * 状态流转：
 * Preview（预览） → Analyzing（分析中） → Result（结果）
 *                                    ↓
 *                                Preview（重新拍摄）
 * 
 * @property recognizeImageUseCase 图像识别用例，调用AI服务
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@HiltViewModel
/**
 * CameraViewModel - Camera视图模型
 * 
 * 功能职责：
 * - 管理Camera界面的业务逻辑
 * - 处理用户交互事件和状态更新
 * - 协调数据层和展示层的通信
 * 
 * 状态管理：
 * - 使用StateFlow管理UI状态
 * - 支持配置变更后的状态保持
 * - 提供状态更新的原子性保证
 * 
 * 生命周期：
 * - 自动处理协程作用域
 * - 支持数据预加载
 * - 优雅的资源清理
 * 
 * @since 1.0.0
 */
class CameraViewModel @Inject constructor(  // 依赖注入
    private val recognizeImageUseCase: RecognizeImageUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<CameraUiState>(CameraUiState.Preview)
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()
    
    /**
     * 分析拍摄的图像
     * 
     * 处理流程：
     * 1. 更新状态为分析中（显示加载动画）
     * 2. 将图片URI转换为文件对象
     * 3. 调用AI图像识别服务
     * 4. 成功时显示识别结果和教育信息
     * 5. 失败时显示友好的降级内容
     * 
     * 降级策略：
     * - AI服务失败时不显示错误
     * - 提供鼓励性的默认回复
     * - 保持教育价值和趣味性
     * 
     * @param imageUri 拍摄图片的URI地址
     * 
     * 异常处理：
     * - 文件访问异常：返回预览状态
     * - 识别失败：显示教育性降级内容
     */
    fun analyzeImage(imageUri: String) {
        viewModelScope.launch {  // 启动协程执行异步操作
            // 更新为分析中状态
            _uiState.value = CameraUiState.Analyzing
            
            try {
                // 将URI转换为文件对象（移除file://前缀）
                val imageFile = File(imageUri.removePrefix("file://"))
                
                // 调用AI图像识别服务
                recognizeImageUseCase(imageFile)
                    .onSuccess { recognitionResult ->
                        // 成功：展示识别结果
                        val displayResult = RecognitionResult(
                            objectName = recognitionResult.objectName,
                            description = recognitionResult.description,
                            funFact = recognitionResult.funFact,
                            confidence = recognitionResult.confidence
                        )
                        _uiState.value = CameraUiState.Result(displayResult)
                    }
                    .onFailure { error ->
                        // 失败：显示教育性的降级内容
                        val fallbackResult = RecognitionResult(
                            objectName = "神秘物品",
                            description = "哎呀，我没有认出这是什么。但是每个物品都有它独特的故事！",
                            funFact = "继续探索，你会发现更多有趣的东西！",
                            confidence = 0.5f
                        )
                        _uiState.value = CameraUiState.Result(fallbackResult)
                    }
            } catch (e: Exception) {  // 捕获并处理异常
                // 文件访问错误：返回预览状态
                _uiState.value = CameraUiState.Preview
            }
        }
    }
    
    /**
     * 重置到预览状态
     * 
     * 用于用户想要重新拍照时调用。
     * 清除当前结果，返回相机预览界面。
     */
    fun resetToPreview() {
        _uiState.value = CameraUiState.Preview
    }
}

/**
 * 相机UI状态
 * 
 * 状态说明：
 * - Preview: 相机预览状态，等待拍照
 * - Analyzing: 正在分析图片，显示加载动画
 * - Result: 显示识别结果和教育信息
 */
/**
 * CameraUiState - 相机UI状态
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用密封类层次设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
sealed class CameraUiState {
    /**
     * Preview - Preview
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Preview : CameraUiState()
    /**
     * Analyzing - Analyzing
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用单例模式设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    object Analyzing : CameraUiState()
    /**
     * Result - Result
     * 
     * 功能描述：
     * - 提供核心业务功能处理功能
     * - 支持灵活配置、易于扩展、高性能
     * 
     * 设计说明：
     * - 采用数据类设计
     * - 遵循项目统一的架构规范
     * 
     * @自版本 1.0.0
     */
    data class Result(val result: RecognitionResult) : CameraUiState()
}

/**
 * 图像识别结果
 * 
 * 包含AI识别的物体信息和相关教育内容。
 * 设计为儿童友好的展示格式。
 * 
 * @property objectName 识别出的物体名称
 * @property description 物体的简单描述，适合儿童理解
 * @property funFact 有趣的知识点，增加教育价值
 * @property confidence 识别置信度（0-1），用于内部判断
 */
/**
 * RecognitionResult - RecognitionResult
 * 
 * 功能描述：
 * - 提供核心业务功能处理功能
 * - 支持灵活配置、易于扩展、高性能
 * 
 * 设计说明：
 * - 采用数据类设计
 * - 遵循项目统一的架构规范
 * 
 * @since 1.0.0
 */
data class RecognitionResult(
    val objectName: String,
    val description: String,
    val funFact: String? = null,
    val confidence: Float
)
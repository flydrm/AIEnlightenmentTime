package com.enlightenment.ai.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.enlightenment.ai.presentation.navigation.EnlightenmentNavHost
import com.enlightenment.ai.presentation.theme.AIEnlightenmentTimeTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 应用主Activity
 * 
 * 职责说明：
 * 作为应用的单一入口Activity，负责初始化UI框架和导航系统。
 * 采用Single Activity架构，所有界面都通过Compose Navigation管理。
 * 
 * 技术特点：
 * 1. 使用Jetpack Compose构建UI
 * 2. 集成Hilt依赖注入
 * 3. 应用自定义主题系统
 * 4. 配置导航控制器
 * 
 * 架构优势：
 * - 单Activity减少内存开销
 * - Navigation组件管理返回栈
 * - Compose声明式UI提高开发效率
 * - Hilt自动管理依赖注入
 * 
 * 生命周期：
 * - onCreate: 设置Compose内容
 * - 其他生命周期由Compose自动管理
 * 
 * @AndroidEntryPoint 标记为Hilt注入点，自动生成依赖注入代码
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Activity创建时调用
     * 
     * 初始化流程：
     * 1. 调用父类onCreate
     * 2. 设置Compose内容
     * 3. 应用自定义主题
     * 4. 创建导航控制器
     * 5. 启动导航宿主
     * 
     * UI结构：
     * - AIEnlightenmentTimeTheme: 应用主题包装器
     * - Surface: Material Design表面容器
     * - EnlightenmentNavHost: 导航路由管理
     * 
     * @param savedInstanceState 保存的实例状态（配置变更时使用）
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 设置Compose UI内容
        setContent {
            // 应用自定义主题，包含颜色、字体、形状等
            AIEnlightenmentTimeTheme {
                // Material Design表面，提供背景色和高度
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // 创建导航控制器，管理界面跳转
                    val navController = rememberNavController()
                    
                    // 导航宿主，定义所有可导航的界面
                    EnlightenmentNavHost(navController = navController)
                }
            }
        }
    }
}
package com.enlightenment.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enlightenment.ai.presentation.home.HomeScreen
import com.enlightenment.ai.presentation.story.StoryScreen
import com.enlightenment.ai.presentation.dialogue.DialogueScreen
import com.enlightenment.ai.presentation.profile.ProfileScreen
import com.enlightenment.ai.presentation.camera.CameraScreen
import com.enlightenment.ai.presentation.parent.ParentLoginScreen
import com.enlightenment.ai.presentation.parent.ParentDashboardScreen
import com.enlightenment.ai.presentation.parent.settings.*

@Composable
fun EnlightenmentNavHost(  // 可组合UI组件
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToStory = {
                    navController.navigate(Screen.Story.route)
                },
                onNavigateToDialogue = {
                    navController.navigate(Screen.Dialogue.route)
                },
                onNavigateToCamera = {
                    navController.navigate(Screen.Camera.route)
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToParentLogin = {
                    navController.navigate(Screen.ParentLogin.route)
                }
            )
        }
        
        composable(Screen.Story.route) {
            StoryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Dialogue.route) {
            DialogueScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Camera.route) {
            CameraScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ParentLogin.route) {
            ParentLoginScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onLoginSuccess = {
                    navController.navigate(Screen.ParentDashboard.route) {
                        popUpTo(Screen.ParentLogin.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.ParentDashboard.route) {
            ParentDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToTimeLimitSettings = {
                    navController.navigate(Screen.TimeLimitSettings.route)
                },
                onNavigateToContentPreferences = {
                    navController.navigate(Screen.ContentPreferences.route)
                },
                onNavigateToDetailedReports = {
                    navController.navigate(Screen.DetailedReports.route)
                },
                onNavigateToPrivacySettings = {
                    navController.navigate(Screen.PrivacySettings.route)
                },
                onNavigateToAppSettings = {
                    navController.navigate(Screen.AppSettings.route)
                }
            )
        }
        
        composable(Screen.TimeLimitSettings.route) {
            TimeLimitSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.ContentPreferences.route) {
            ContentPreferencesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.DetailedReports.route) {
            DetailedReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.AppSettings.route) {
            AppSettingsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
/**
 * 界面组件
 * 
 * 界面功能：
 * 提供相关的用户界面和交互功能。
 * 遵循Material Design设计规范，支持响应式布局。
 * 
 * 技术架构：
 * - Jetpack Compose构建UI
 * - MVVM架构模式
 * - StateFlow状态管理
 * - Hilt依赖注入
 * 
 * 用户体验：
 * - 流畅的动画效果
 * - 即时的交互反馈
 * - 友好的错误提示
 * - 无障碍功能支持
 * 
 * @Composable Compose函数标注
 * 
 * @author AI启蒙时光团队
 * @since 1.0.0
 */


/**
 * 界面 - 屏幕路由
 * 
 * 提供用户界面展示和交互功能
 * 
 * @自版本 1.0.0
 */
sealed class Screen(val route: String) {
/**
 * Home
 * 
 * 功能说明：
 * 提供Home相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * Home - Home
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
    object Home : Screen("home")
/**
 * 故事
 * 
 * 功能说明：
 * 提供Story相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 故事 - 故事
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
    object Story : Screen("story")
/**
 * 对话
 * 
 * 功能说明：
 * 提供Dialogue相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 对话 - 对话
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
    object Dialogue : Screen("dialogue")
/**
 * 档案
 * 
 * 功能说明：
 * 提供Profile相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 档案 - 档案
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
    object Profile : Screen("profile")
/**
 * 相机
 * 
 * 功能说明：
 * 提供Camera相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * 相机 - 相机
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
    object Camera : Screen("camera")
/**
 * ParentLogin
 * 
 * 功能说明：
 * 提供ParentLogin相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * ParentLogin - ParentLogin
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
    object ParentLogin : Screen("parent_login")
/**
 * ParentDashboard
 * 
 * 功能说明：
 * 提供ParentDashboard相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * ParentDashboard - ParentDashboard
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
    object ParentDashboard : Screen("parent_dashboard")
/**
 * TimeLimitSettings
 * 
 * 功能说明：
 * 提供TimeLimitSettings相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * TimeLimitSettings - TimeLimitSettings
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
    object TimeLimitSettings : Screen("time_limit_settings")
/**
 * ContentPreferences
 * 
 * 功能说明：
 * 提供ContentPreferences相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * ContentPreferences - ContentPreferences
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
    object ContentPreferences : Screen("content_preferences")
/**
 * DetailedReports
 * 
 * 功能说明：
 * 提供DetailedReports相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * DetailedReports - DetailedReports
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
    object DetailedReports : Screen("detailed_reports")
/**
 * PrivacySettings
 * 
 * 功能说明：
 * 提供PrivacySettings相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * PrivacySettings - PrivacySettings
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
    object PrivacySettings : Screen("privacy_settings")
/**
 * AppSettings
 * 
 * 功能说明：
 * 提供AppSettings相关的功能实现。
 * 
 * 技术特点：
 * - 遵循SOLID原则
 * - 支持依赖注入
 * - 线程安全设计
 * 
 * @author AI启蒙时光团队
 * @自版本 1.0.0
 */

    /**
     * AppSettings - AppSettings
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
    object AppSettings : Screen("app_settings")
}
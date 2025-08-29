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
fun EnlightenmentNavHost(
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

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Story : Screen("story")
    object Dialogue : Screen("dialogue")
    object Profile : Screen("profile")
    object Camera : Screen("camera")
    object ParentLogin : Screen("parent_login")
    object ParentDashboard : Screen("parent_dashboard")
    object TimeLimitSettings : Screen("time_limit_settings")
    object ContentPreferences : Screen("content_preferences")
    object DetailedReports : Screen("detailed_reports")
    object PrivacySettings : Screen("privacy_settings")
    object AppSettings : Screen("app_settings")
}
package com.enlightenment.ai.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.enlightenment.ai.presentation.home.HomeScreen
import com.enlightenment.ai.presentation.story.StoryScreen
import com.enlightenment.ai.presentation.dialogue.DialogueScreen
import com.enlightenment.ai.presentation.profile.ProfileScreen

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
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
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
    }
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Story : Screen("story")
    object Dialogue : Screen("dialogue")
    object Profile : Screen("profile")
}
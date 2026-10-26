package com.example.geotask_app

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.geotask_app.ui.MainViewModel

@Composable
fun AppNavigation(viewModel: MainViewModel, isLoggedIn: Boolean) {
    val navController = rememberNavController()

    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("home") { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "home" else "login",
        // Tüm sayfalara harika bir kayma ve solma animasyonu ekliyoruz
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeIn(animationSpec = tween(400))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(400)) + fadeOut(animationSpec = tween(400))
        }
    ) {
        composable("login") {
            LoginScreen(viewModel = viewModel)
        }
        composable("home") {
            com.example.geotask_app.ui.TasksScreen(
                viewModel = viewModel,
                onNavigateToAddTask = { navController.navigate("addTask") },
                onNavigateToDetail = { taskId -> navController.navigate("taskDetail/$taskId") }
            )
        }
        composable("addTask") {
            com.example.geotask_app.ui.AddTaskScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("taskDetail/{taskId}") { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            if (taskId != null) {
                com.example.geotask_app.ui.TaskDetailScreen(
                    taskId = taskId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

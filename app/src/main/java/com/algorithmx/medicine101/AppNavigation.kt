package com.algorithmx.medicine101

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.algorithmx.medicine101.ui.screens.NoteScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerScreen
import com.algorithmx.medicine101.ui.screens.search.SearchScreen


@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "explorer_root" ,// Start at the new Explorer
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        }
    ) {
        composable("search") {
            SearchScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNote = { noteId -> navController.navigate("editor/$noteId") },
                onNavigateToFolder = { folderId -> navController.navigate("explorer/$folderId") }
            )
        }
        // 1. Root Explorer (Home)
        composable("explorer_root") {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId ->
                    navController.navigate("editor/$noteId") // <--- CONNECTED!
                },onSearchClick = { navController.navigate("search") }
            )
        }

        // 2. Folder Explorer (Deep Navigation)
        composable(
            route = "explorer/{folderId}",
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId ->
                    navController.navigate("editor/$noteId") // <--- CONNECTED!
                },onSearchClick = { navController.navigate("search") }
            )
        }
        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteScreen(
                onBack = { navController.popBackStack() }
            )
        }


        // ... (Keep your old routes if you want, or delete CaseListScreen routes) ...
    }
}
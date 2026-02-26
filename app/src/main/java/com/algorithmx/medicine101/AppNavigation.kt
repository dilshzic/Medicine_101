package com.algorithmx.medicine101

import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.ui.auth.AuthViewModel
import com.algorithmx.medicine101.ui.auth.LoginScreen
import com.algorithmx.medicine101.ui.screens.NoteScreen
import com.algorithmx.medicine101.ui.screens.dashboard.DashboardScreen
import com.algorithmx.medicine101.ui.screens.pdfviewer.PdfViewerScreen
import com.algorithmx.medicine101.ui.screens.pdfviewer.TocPdfViewerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerViewModel
import com.algorithmx.medicine101.ui.screens.profile.ProfileScreen
import com.algorithmx.medicine101.ui.screens.search.SearchScreen
import com.algorithmx.medicine101.ui.screens.brain.BrainManagerScreen

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val user by authViewModel.user.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (user == null) "login" else "dashboard",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
    ) {
        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate("dashboard") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") },
                onExplorerClick = { navController.navigate("explorer_root") },
                onProfileClick = { navController.navigate("profile") },
                onBrainClick = { navController.navigate("brain_manager") }
            )
        }

        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("dashboard") { inclusive = true }
                    }
                }
            )
        }

        composable("brain_manager") {
            BrainManagerScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("explorer_root") {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "explorer/{folderId}",
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") },
                onBack = { navController.popBackStack() }
            )
        }

        composable("search") {
            SearchScreen(
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "note_screen/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable
            val explorerViewModel: ExplorerViewModel = hiltViewModel()
            var noteMetadata by remember { mutableStateOf<NoteEntity?>(null) }
            var isLoading by remember { mutableStateOf(true) }

            LaunchedEffect(noteId) {
                noteMetadata = explorerViewModel.getNoteById(noteId)
                isLoading = false
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                noteMetadata?.let { note ->
                    if (note.pdfUri != null) {
                        val page = note.pdfPage ?: 0

                        if (page > 0) {
                            TocPdfViewerScreen(
                                pdfPath = note.pdfUri,
                                initialPage = page,
                                onBack = { navController.popBackStack() }
                            )
                        } else {
                            PdfViewerScreen(
                                pdfPath = note.pdfUri,
                                initialPage = 0,
                                onBack = { navController.popBackStack() }
                            )
                        }
                    } else {
                        NoteScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToNote = { linkedId ->
                                navController.navigate("note_screen/$linkedId")
                            }
                        )
                    }
                }
            }
        }

        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteScreen(
                onBack = { navController.popBackStack() },
                onNavigateToNote = { linkedId ->
                    navController.navigate("note_screen/$linkedId")
                }
            )
        }
    }
}

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
import com.algorithmx.medicine101.ui.screens.NoteScreen
import com.algorithmx.medicine101.ui.screens.dashboard.DashboardScreen
import com.algorithmx.medicine101.ui.screens.pdfviewer.PdfViewerScreen
import com.algorithmx.medicine101.ui.screens.pdfviewer.TocPdfViewerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerViewModel

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
    ) {
        composable("dashboard") {
            DashboardScreen(
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") },
                onExplorerClick = { navController.navigate("explorer_root") }
            )
        }

        composable("explorer_root") {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") }
            )
        }

        composable(
            route = "explorer/{folderId}",
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId -> navController.navigate("note_screen/$noteId") },
                onSearchClick = { navController.navigate("search") }
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
                        NoteScreen(onBack = { navController.popBackStack() })
                    }
                }
            }
        }

        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteScreen(onBack = { navController.popBackStack() })
        }
    }
}
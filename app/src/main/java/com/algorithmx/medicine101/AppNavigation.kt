package com.algorithmx.medicine101

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.ui.screens.NoteScreen
import com.algorithmx.medicine101.ui.screens.PdfViewerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerScreen
import com.algorithmx.medicine101.ui.screens.folders.ExplorerViewModel
// import com.algorithmx.medicine101.ui.screens.search.SearchScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "explorer_root", // Start at the root Explorer
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
        // 1. Root Explorer (Home)
        composable("explorer_root") {
            ExplorerScreen(
                onFolderClick = { navController.navigate("explorer/$it") },
                onNoteClick = { noteId ->
                    navController.navigate("note_screen/$noteId") // Route to the Smart Handler
                },
                onSearchClick = { navController.navigate("search") }
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
                    navController.navigate("note_screen/$noteId") // Route to the Smart Handler
                },
                onSearchClick = { navController.navigate("search") }
            )
        }

        // 3. Smart Note Handler (Decides between JSON Note or PDF Viewer)
        composable(
            route = "note_screen/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId") ?: return@composable

            // Get ViewModel to fetch note data safely within Compose
            val explorerViewModel: ExplorerViewModel = hiltViewModel()
            var noteMetadata by remember { mutableStateOf<NoteEntity?>(null) }

            // Fetch note details asynchronously so it doesn't freeze the UI
            LaunchedEffect(noteId) {
                noteMetadata = explorerViewModel.getNoteById(noteId)
            }

            // Wait until the note is loaded from DB, then render the correct screen
            noteMetadata?.let { note ->
                if (note.pdfUri != null) {
                    PdfViewerScreen(
                        pdfPath = note.pdfUri,
                        initialPage = note.pdfPage ?: 0,
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    NoteScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }

        // 4. Direct Editor Route (Used if explicitly calling the editor)
        composable(
            route = "editor/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) {
            NoteScreen(onBack = { navController.popBackStack() })
        }
    }
}
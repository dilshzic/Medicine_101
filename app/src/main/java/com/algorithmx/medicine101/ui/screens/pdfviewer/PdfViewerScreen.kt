package com.algorithmx.medicine101.ui.screens.pdfviewer

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
// NEW IMPORT: Brings in the Editable version of the viewer
import androidx.pdf.ink.EditablePdfViewerFragment
import com.algorithmx.medicine101.databinding.FragmentPdfViewerBinding
import java.io.File


// --- 1. THE CUSTOM FRAGMENT ---
// Inheriting from EditablePdfViewerFragment unlocks Annotations & Saving
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 18)
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
class MedMatePdfFragment : EditablePdfViewerFragment() {

    // Callbacks to communicate back to Compose
    var onImmersiveModeToggle: ((Boolean) -> Unit)? = null
    var onDocumentLoaded: (() -> Unit)? = null

    override fun onRequestImmersiveMode(enterImmersive: Boolean) {
        super.onRequestImmersiveMode(enterImmersive)
        // The user tapped the page to read/view in full screen.
        // We tell Compose to hide our TopAppBar.
        onImmersiveModeToggle?.invoke(enterImmersive)
    }

    override fun onLoadDocumentSuccess() {
        super.onLoadDocumentSuccess()
        // The document is ready. We tell Compose it is now safe to enable Search.
        onDocumentLoaded?.invoke()
    }

    /*
    @OptIn(androidx.pdf.ExperimentalPdfApi::class)
    override fun onPdfViewCreated(pdfView: androidx.pdf.PdfView) {
        super.onPdfViewCreated(pdfView)
        // NOTE ON PAGE NAVIGATION:
        // This is where the 'initialPage' logic belongs.
        // Once the Android Jetpack team exposes the 'scrollToPage()' method
        // on 'PdfView' in a future update, you execute it right here.
    }
    */
}


// --- 2. THE COMPOSE SCREEN ---
@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfPath: String,
    initialPage: Int, // Stored for future navigation API
    onBack: () -> Unit
) {
    val displayTitle = File(pdfPath).nameWithoutExtension

    // UI States
    var isSearchActive by remember { mutableStateOf(false) }
    var isDocumentLoaded by remember { mutableStateOf(false) }
    var isImmersiveMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            // Feature: Immersive Mode
            // Hide the TopAppBar smoothly when the fragment requests immersive reading
            AnimatedVisibility(
                visible = !isImmersiveMode,
                enter = slideInVertically(initialOffsetY = { -it }),
                exit = slideOutVertically(targetOffsetY = { -it })
            ) {
                TopAppBar(
                    title = { Text(text = displayTitle, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        // Feature: Search
                        // Disable the button until the document is loaded (per documentation rules)
                        IconButton(
                            onClick = { isSearchActive = !isSearchActive },
                            enabled = isDocumentLoaded
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                // Highlight the icon if search is currently active
                                tint = if (isSearchActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    ) { padding ->
        AndroidViewBinding(
            factory = FragmentPdfViewerBinding::inflate,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            val context = root.context
            val fragmentManager = (context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
                ?: (context as? android.view.ContextThemeWrapper)?.baseContext?.let { it as? androidx.fragment.app.FragmentActivity }?.supportFragmentManager
                ?: throw IllegalStateException("Context is not a FragmentActivity.")

            // Look for our custom subclass
            var fragment = fragmentManager.findFragmentById(this.pdfContainer.id) as? MedMatePdfFragment

            if (fragment == null) {
                fragment = MedMatePdfFragment()
                fragmentManager.beginTransaction()
                    .replace(this.pdfContainer.id, fragment)
                    .commitNowAllowingStateLoss()
            }

            // Bind the fragment's callbacks to our Compose states
            fragment.onImmersiveModeToggle = { immersive ->
                isImmersiveMode = immersive
            }
            fragment.onDocumentLoaded = {
                isDocumentLoaded = true
            }

            val newUri = Uri.fromFile(File(pdfPath))

            // Set URI only if it changed (prevents the "reload to page 0" bug)
            if (fragment.documentUri != newUri) {
                isDocumentLoaded = false
                isSearchActive = false
                fragment.documentUri = newUri
            }

            // Feature: Sync Search State
            // The documentation specifically states this can ONLY be set after LoadDocumentSuccess
            if (isDocumentLoaded) {
                if (fragment.isTextSearchActive != isSearchActive) {
                    fragment.isTextSearchActive = isSearchActive
                }
            }

            // Feature: Annotations (FAB) & Saving
            // Handled automatically by EditablePdfViewerFragment.
            // When you draw and hit the back button, it will intercept the press
            // to show the Save/Discard dialog.
        }
    }
}
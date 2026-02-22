package com.algorithmx.medicine101.ui.screens.pdfviewer

import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.pdf.viewer.fragment.PdfViewerFragment
import com.algorithmx.medicine101.databinding.FragmentPdfViewerBinding
import java.io.File

@RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfViewerScreen(
    pdfPath: String,
    initialPage: Int,
    onBack: () -> Unit
) {
    val displayTitle = File(pdfPath).nameWithoutExtension

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = displayTitle,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        AndroidViewBinding(
            factory = FragmentPdfViewerBinding::inflate,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // --- INTEGRATED ROBUST FRAGMENT MANAGER LOGIC ---
            val context = root.context
            val fragmentManager = (context as? androidx.fragment.app.FragmentActivity)?.supportFragmentManager
                ?: (context as? android.view.ContextThemeWrapper)?.baseContext?.let { it as? androidx.fragment.app.FragmentActivity }?.supportFragmentManager
                ?: throw IllegalStateException("Context is not a FragmentActivity. Ensure MainActivity extends FragmentActivity.")

            // Find the fragment by the ID of the FragmentContainerView in your XML
            val fragment = fragmentManager.findFragmentById(this.pdfContainer.id) as? PdfViewerFragment

            // Load the document
            fragment?.documentUri = Uri.fromFile(File(pdfPath))

            // Post-layout logic for page jumping (if API supports it in your alpha build)
            this.root.post {
                try {
                    // Placeholder for future stable page jumping:
                    // fragment?.scrollToPage(initialPage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
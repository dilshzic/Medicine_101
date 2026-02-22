package com.algorithmx.medicine101.ui.screens

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
    // Extract a readable title from the file path to show in the TopBar
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
            // ViewBinding automatically converts snake_case XML IDs to camelCase properties.
            // If your XML ID is "@+id/pdf_container", use `this.pdfContainer`
            val fragment = this.pdfContainer.getFragment<PdfViewerFragment>()

            // 1. Assign the PDF file URI to the viewer
            fragment.documentUri = Uri.fromFile(File(pdfPath))

            // 2. Handle Jump to initialPage
            // NOTE: Because androidx.pdf is still in Alpha, the API for scrolling
            // is actively evolving. In some alpha builds, you might have to wait
            // for the view to lay out before attempting to scroll.
            this.root.post {
                try {
                    // Check if your specific alpha version exposes a scroll/jump method.
                    // If it does not yet expose 'scrollToPage', this acts as a placeholder
                    // where that logic will go once the library reaches stable Beta.
                    // fragment.scrollToPage(initialPage)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}
package com.algorithmx.medicine101.ui.screens.pdfviewer

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.barteksc.pdfviewer.PDFView
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TocPdfViewerScreen(
    pdfPath: String,
    initialPage: Int,
    onBack: () -> Unit
) {
    val displayTitle = File(pdfPath).nameWithoutExtension

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = displayTitle, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
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
        AndroidView(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            factory = { context ->
                PDFView(context, null) // Initialize legacy PDFView
            },
            update = { pdfView ->
                val file = File(pdfPath)
                if (file.exists()) {
                    pdfView.fromFile(file)
                        .defaultPage(initialPage) // JUMPS TO YOUR TOC PAGE!
                        .enableSwipe(true)
                        .swipeHorizontal(false)
                        .enableDoubletap(true)
                        .enableAnnotationRendering(true) // Renders highlights made by Jetpack
                        .load()
                }
            }
        )
    }
}
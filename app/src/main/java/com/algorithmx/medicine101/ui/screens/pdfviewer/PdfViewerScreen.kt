package com.algorithmx.medicine101.ui.screens.pdfviewer

import android.net.Uri
import android.os.Build
import android.view.View
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.pdf.viewer.fragment.PdfViewerFragment
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
                title = { Text(displayTitle, maxLines = 1, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
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
            modifier = Modifier.padding(padding).fillMaxSize(),
            factory = { context ->
                // Create the container dynamically
                FragmentContainerView(context).apply {
                    id = View.generateViewId()
                }
            },
            update = { view ->
                val fm = (view.context as FragmentActivity).supportFragmentManager
                var fragment = fm.findFragmentById(view.id) as? PdfViewerFragment

                // Attach the native viewer if it isn't attached yet
                if (fragment == null) {
                    fragment = PdfViewerFragment()
                    fm.beginTransaction().replace(view.id, fragment).commitNowAllowingStateLoss()
                }

                // Load the URI
                fragment.documentUri = Uri.fromFile(File(pdfPath))
            }
        )
    }
}
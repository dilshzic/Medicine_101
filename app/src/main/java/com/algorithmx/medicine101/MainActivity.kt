package com.algorithmx.medicine101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.algorithmx.medicine101.ui.theme.Medicine101Theme
import dagger.hilt.android.AndroidEntryPoint
// CRITICAL IMPORT:
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- THE CORRECT INITIALIZATION ---
        PDFBoxResourceLoader.init(applicationContext)

        mainViewModel.triggerSeeding()

        enableEdgeToEdge()
        setContent {
            Medicine101Theme {
                AppNavigation()
            }
        }
    }
}
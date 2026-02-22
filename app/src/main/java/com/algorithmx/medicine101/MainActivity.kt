package com.algorithmx.medicine101

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels // <-- Ensure this is imported
import com.algorithmx.medicine101.ui.theme.Medicine101Theme
import dagger.hilt.android.AndroidEntryPoint
import com.tom_roush.pdfbox.android.PDFBox

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // Declare the ViewModel
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- CRITICAL FIX: EXPLICITLY TRIGGER IT ---
        mainViewModel.triggerSeeding()
        PDFBox.setup(applicationContext)

        enableEdgeToEdge()
        setContent {
            Medicine101Theme {
                AppNavigation()
            }
        }
    }
}
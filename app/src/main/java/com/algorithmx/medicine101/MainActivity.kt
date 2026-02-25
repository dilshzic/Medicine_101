package com.algorithmx.medicine101

import android.os.Build
import android.os.Bundle
// 1. Change this import
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresExtension
import com.algorithmx.medicine101.ui.theme.Medicine101Theme
import com.algorithmx.medicine101.flowchart.ml.RecognitionManager
import dagger.hilt.android.AndroidEntryPoint
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader

@AndroidEntryPoint
// 2. Change the class declaration to extend FragmentActivity
class MainActivity : FragmentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    @RequiresExtension(extension = Build.VERSION_CODES.S, version = 13)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize PDFBox
        PDFBoxResourceLoader.init(applicationContext)
        RecognitionManager.initialize()

        mainViewModel.triggerSeeding()

        enableEdgeToEdge()
        setContent {
            Medicine101Theme {
                AppNavigation()
            }
        }
    }
}
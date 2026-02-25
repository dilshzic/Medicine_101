//package com.medmate.flowchart.ml
//
//import android.util.Log
//import com.google.mlkit.common.model.DownloadConditions
//import com.google.mlkit.common.model.RemoteModelManager
//import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModel
//import com.google.mlkit.vision.digitalink.DigitalInkRecognitionModelIdentifier
//
//object RecognitionManager {
//    private const val TAG = "RecognitionManager"
//
//    // The "Autodraw" model recognizes shapes like rectangles, circles, diamonds
//    private val modelIdentifier: DigitalInkRecognitionModelIdentifier? =
//        DigitalInkRecognitionModelIdentifier.fromLanguageTag("zxx-Zsym-x-autodraw")
//
//    private var model: DigitalInkRecognitionModel? = null
//
//    fun initialize() {
//        if (modelIdentifier == null) {
//            Log.e(TAG, "Model Identifier failed to load")
//            return
//        }
//
//        model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
//        downloadModel()
//    }
//
//    private fun downloadModel() {
//        val m = model ?: return
//        val remoteModelManager = RemoteModelManager.getInstance()
//
//        remoteModelManager.isModelDownloaded(m)
//            .addOnSuccessListener { isDownloaded ->
//                if (!isDownloaded) {
//                    Log.d(TAG, "Model not found. Downloading...")
//                    val conditions = DownloadConditions.Builder().build()
//                    remoteModelManager.download(m, conditions)
//                        .addOnSuccessListener { Log.d(TAG, "Model downloaded successfully") }
//                        .addOnFailureListener { e -> Log.e(TAG, "Model download failed", e) }
//                } else {
//                    Log.d(TAG, "Model already exists.")
//                }
//            }
//    }
//}

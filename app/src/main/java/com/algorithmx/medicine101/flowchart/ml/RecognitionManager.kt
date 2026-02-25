package com.algorithmx.medicine101.flowchart.ml

import android.util.Log
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink

object RecognitionManager {
    private const val TAG = "RecognitionManager"

    // The "Autodraw" model recognizes shapes like rectangles, circles, diamonds
    private val modelIdentifier: DigitalInkRecognitionModelIdentifier? =
        DigitalInkRecognitionModelIdentifier.fromLanguageTag("zxx-Zsym-x-autodraw")

    private var model: DigitalInkRecognitionModel? = null

    // Text model for handwriting-to-text
    private val textModelIdentifier: DigitalInkRecognitionModelIdentifier? =
        DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")

    private var textModel: DigitalInkRecognitionModel? = null

    fun initialize() {
        if (modelIdentifier == null) {
            Log.e(TAG, "Shape Model Identifier failed to load")
        } else {
            model = DigitalInkRecognitionModel.builder(modelIdentifier).build()
            downloadModel()
        }

        if (textModelIdentifier == null) {
            Log.e(TAG, "Text Model Identifier failed to load")
        } else {
            textModel = DigitalInkRecognitionModel.builder(textModelIdentifier).build()
            downloadTextModel()
        }
    }

    private fun downloadModel() {
        val m = model ?: return
        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.isModelDownloaded(m)
            .addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    Log.d(TAG, "Shape model not found. Downloading...")
                    val conditions = DownloadConditions.Builder().build()
                    remoteModelManager.download(m, conditions)
                        .addOnSuccessListener { Log.d(TAG, "Shape model downloaded successfully") }
                        .addOnFailureListener { e -> Log.e(TAG, "Shape model download failed", e) }
                } else {
                    Log.d(TAG, "Shape model already exists.")
                }
            }
    }

    private fun downloadTextModel() {
        val m = textModel ?: return
        val remoteModelManager = RemoteModelManager.getInstance()

        remoteModelManager.isModelDownloaded(m)
            .addOnSuccessListener { isDownloaded ->
                if (!isDownloaded) {
                    Log.d(TAG, "Text model not found. Downloading...")
                    val conditions = DownloadConditions.Builder().build()
                    remoteModelManager.download(m, conditions)
                        .addOnSuccessListener { Log.d(TAG, "Text model downloaded successfully") }
                        .addOnFailureListener { e -> Log.e(TAG, "Text model download failed", e) }
                } else {
                    Log.d(TAG, "Text model already exists.")
                }
            }
    }

    fun recognizeShape(
        ink: Ink,
        onResult: (String, Float) -> Unit
    ) {
        val m = model ?: return
        val recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(m).build()
        )

        recognizer.recognize(ink)
            .addOnSuccessListener { result ->
                if (result.candidates.isNotEmpty()) {
                    val topCandidate = result.candidates[0]
                    onResult(topCandidate.text, topCandidate.score ?: 0f)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Shape recognition failed", e)
            }
    }

    fun recognizeText(ink: Ink, onResult: (String) -> Unit) {
        val m = textModel ?: return
        val recognizer = DigitalInkRecognition.getClient(
            DigitalInkRecognizerOptions.builder(m).build()
        )

        recognizer.recognize(ink)
            .addOnSuccessListener { result ->
                if (result.candidates.isNotEmpty()) {
                    onResult(result.candidates[0].text)
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Text recognition failed", e)
            }
    }
}

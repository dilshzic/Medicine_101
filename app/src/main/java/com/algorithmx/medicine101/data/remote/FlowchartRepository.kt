package com.algorithmx.medicine101.data.remote

import android.content.Context
import com.algorithmx.medicine101.data.FlowchartFile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FlowchartRepository @Inject constructor(@ApplicationContext private val context: Context) {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }

    suspend fun saveFlowchart(flowchart: FlowchartFile) = withContext(Dispatchers.IO) {
        val filename = "${flowchart.id}.json"
        val file = File(context.filesDir, filename)
        
        val jsonString = json.encodeToString(flowchart)
        file.writeText(jsonString)
    }

    suspend fun loadFlowchart(id: String): FlowchartFile? = withContext(Dispatchers.IO) {
        val filename = "$id.json"
        val file = File(context.filesDir, filename)
        
        if (!file.exists()) return@withContext null

        try {
            val jsonString = file.readText()
            json.decodeFromString<FlowchartFile>(jsonString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun getAllFlowcharts(): List<FlowchartFile> = withContext(Dispatchers.IO) {
        val files = context.filesDir.listFiles { _, name -> name.endsWith(".json") }
        files?.mapNotNull { file ->
            try {
                json.decodeFromString<FlowchartFile>(file.readText())
            } catch (e: Exception) {
                null
            }
        } ?: emptyList()
    }
}

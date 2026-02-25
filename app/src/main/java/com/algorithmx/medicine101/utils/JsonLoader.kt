package com.algorithmx.medicine101.utils

import android.content.Context
import com.algorithmx.medicine101.data.ContentBlock
import kotlinx.serialization.json.Json
import java.io.IOException

object JsonLoader {
    private val json = Json { ignoreUnknownKeys = true }

    // Load the Menu (TOC)
    fun loadMenu(context: Context, tocFile: String): List<TocItem> {
        val jsonStr = loadJsonFromAsset(context, tocFile) ?: return emptyList()
        return try {
            json.decodeFromString<List<TocItem>>(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Load a Chapter (Content Blocks)
    fun loadChapter(context: Context, fileName: String): List<ContentBlock> {
        val jsonStr = loadJsonFromAsset(context, fileName) ?: return emptyList()
        return try {
            json.decodeFromString<List<ContentBlock>>(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun loadJsonFromAsset(context: Context, fileName: String): String? {
        return try {
            context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}

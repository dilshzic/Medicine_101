package com.algorithmx.medicine101.utils

import android.content.Context
import com.algorithmx.medicine101.data.ContentBlock
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

object JsonLoader {

    // Load the Menu (TOC)
    fun loadMenu(context: Context, tocFile: String): List<TocItem> {
        val json = loadJsonFromAsset(context, tocFile) ?: return emptyList()
        val type = object : TypeToken<List<TocItem>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Load a Chapter (Content Blocks)
    fun loadChapter(context: Context, fileName: String): List<ContentBlock> {
        val json = loadJsonFromAsset(context, fileName) ?: return emptyList()
        val type = object : TypeToken<List<ContentBlock>>() {}.type
        return Gson().fromJson(json, type)
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
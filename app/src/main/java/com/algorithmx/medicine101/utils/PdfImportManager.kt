package com.algorithmx.medicine101.utils

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

// --- NEW PDFBox Imports ---
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination

class PdfImportManager @Inject constructor(
    private val repository: NoteRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun importPdf(uri: Uri, defaultFileName: String) = withContext(Dispatchers.IO) {
        // Extract real filename from Uri
        val realFileName = getFileNameFromUri(uri) ?: defaultFileName
        val displayName = realFileName.removeSuffix(".pdf").removeSuffix(".PDF")

        // 1. Copy PDF to internal storage so the app doesn't lose permission to read it
        val internalFile = File(context.filesDir, "pdfs/${UUID.randomUUID()}.pdf")
        internalFile.parentFile?.mkdirs()
        context.contentResolver.openInputStream(uri)?.use { input ->
            internalFile.outputStream().use { output -> input.copyTo(output) }
        }

        // 2. Load with PDFBox to extract TOC
        PDDocument.load(internalFile).use { document ->
            val rootFolderId = UUID.randomUUID().toString()

            // Create the Root Folder (The Book)
            repository.insertNote(
                NoteEntity(
                    id = rootFolderId,
                    title = displayName,
                    category = "Textbook",
                    isFolder = true,
                    parentId = null, 
                    isSystemNote = false,
                    pdfUri = internalFile.absolutePath
                )
            )

            val outline = document.documentCatalog.documentOutline
            if (outline != null) {
                mapOutlineNode(outline, rootFolderId, internalFile.absolutePath, document)
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

    private suspend fun mapOutlineNode(
        node: PDOutlineNode,
        parentId: String,
        pdfPath: String,
        doc: PDDocument
    ) {
        var current: PDOutlineItem? = node.firstChild

        while (current != null) {
            val noteId = UUID.randomUUID().toString()
            val hasChildren = current.firstChild != null

            var dest = current.destination
            if (dest == null && current.action is PDActionGoTo) {
                dest = (current.action as PDActionGoTo).destination
            }

            val page = dest?.let { d ->
                if (d is PDPageDestination) {
                    doc.pages.indexOf(d.page)
                } else null
            } ?: 0

            repository.insertNote(NoteEntity(
                id = noteId,
                title = current.title ?: "Untitled Section",
                category = "Textbook",
                isFolder = hasChildren,
                parentId = parentId,
                isSystemNote = false,
                pdfUri = pdfPath,
                pdfPage = page
            ))

            if (hasChildren) {
                mapOutlineNode(current, noteId, pdfPath, doc)
            }

            current = current.nextSibling
        }
    }
}
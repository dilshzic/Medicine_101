package com.algorithmx.medicine101.utils

import android.content.Context
import android.net.Uri
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject // Make sure to use javax.inject for Hilt

// --- NEW PDFBox Imports ---
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.interactive.action.PDActionGoTo
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.destination.PDPageDestination
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineItem
import com.tom_roush.pdfbox.pdmodel.interactive.documentnavigation.outline.PDOutlineNode

class PdfImportManager @Inject constructor(
    private val repository: NoteRepository,
    @ApplicationContext private val context: Context
) {
    suspend fun importPdf(uri: Uri, fileName: String) = withContext(Dispatchers.IO) {
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
                    title = fileName,
                    category = "Textbook",
                    isFolder = true,
                    parentId = null, // Or a specific 'Library' parentId
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

    private suspend fun mapOutlineNode(
        node: PDOutlineNode,
        parentId: String,
        pdfPath: String,
        doc: PDDocument
    ) {
        // node.firstChild is explicitly a PDOutlineItem
        var current: PDOutlineItem? = node.firstChild

        while (current != null) {
            val noteId = UUID.randomUUID().toString()
            val hasChildren = current.firstChild != null

            // --- CRITICAL FIX: Extracting the Destination ---
            // Some PDFs store the destination directly, others wrap it in a GoTo Action.
            var dest = current.destination
            if (dest == null && current.action is PDActionGoTo) {
                dest = (current.action as PDActionGoTo).destination
            }

            // Find the page number for this TOC entry (0-indexed)
            val page = dest?.let { d ->
                if (d is PDPageDestination) {
                    doc.pages.indexOf(d.page)
                } else null
            } ?: 0

            // Save the node to the database
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

            // Recursively map any sub-chapters
            if (hasChildren) {
                mapOutlineNode(current, noteId, pdfPath, doc)
            }

            // Move to the next chapter at this level
            current = current.nextSibling
        }
    }
}
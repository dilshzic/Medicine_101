package com.algorithmx.medicine101.data.remote

import android.util.Log
import com.algorithmx.medicine101.data.ContentBlockEntity
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import com.google.firebase.auth.FirebaseAuth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val localRepository: NoteRepository,
    private val cloudRepository: CloudSyncRepository,
    private val auth: FirebaseAuth
) {
    private val TAG = "SyncManager"

    suspend fun syncEverything(): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        Log.d(TAG, "Sync started for user: $uid")
        
        // 1. Pull from Cloud
        val cloudResult = cloudRepository.pullNotesFromCloud()
        if (cloudResult.isFailure) return Result.failure(cloudResult.exceptionOrNull()!!)

        val cloudItems = cloudResult.getOrNull() ?: emptyList()
        
        Log.d(TAG, "Pulled ${cloudItems.size} notes from cloud.")

        // 2. Sort to avoid Foreign Key violations (Folders first)
        val sortedCloudItems = cloudItems.sortedByDescending { it.first.isFolder }

        for ((cloudNote, cloudBlocks) in sortedCloudItems) {
            val localNote = localRepository.getNoteById(cloudNote.id)

            when {
                // Scenario A: Local missing or Cloud is strictly newer
                localNote == null || cloudNote.updatedAt > localNote.updatedAt -> {
                    Log.d(TAG, "Syncing Cloud -> Local: '${cloudNote.title}' (${cloudBlocks.size} blocks)")
                    
                    val noteEntity = NoteEntity(
                        id = cloudNote.id,
                        title = cloudNote.title,
                        category = cloudNote.category,
                        tags = cloudNote.tags,
                        type = cloudNote.type,
                        isFolder = cloudNote.isFolder,
                        parentId = cloudNote.parentId,
                        sortOrder = cloudNote.sortOrder,
                        isSystemNote = cloudNote.isSystemNote,
                        lastModified = cloudNote.lastModified,
                        isPinned = cloudNote.isPinned,
                        createdAt = cloudNote.createdAt,
                        updatedAt = cloudNote.updatedAt,
                        isDeleted = cloudNote.isDeleted,
                        pdfUri = cloudNote.pdfUri,
                        pdfPage = cloudNote.pdfPage
                    )
                    localRepository.insertNote(noteEntity)
                    
                    val blockEntities = cloudBlocks.map { cb ->
                        val numericId = cb.blockId.toLongOrNull() ?: cb.blockId.hashCode().toLong()
                        
                        ContentBlockEntity(
                            blockId = if (numericId == 0L) 0L else numericId,
                            noteId = cloudNote.id,
                            type = cb.type,
                            // CRITICAL: Ensure content is treated as a RAW string to prevent JSON corruption
                            content = cb.content, 
                            orderIndex = cb.orderIndex,
                            tabName = cb.tabName
                        )
                    }
                    
                    localRepository.syncBlocks(cloudNote.id, blockEntities)
                }
                
                // Scenario B: Local is strictly newer -> Push to cloud
                localNote.updatedAt > cloudNote.updatedAt -> {
                    Log.d(TAG, "Syncing Local -> Cloud: '${localNote.title}'")
                    val noteWithBlocks = localRepository.getNoteWithBlocks(localNote.id)
                    if (noteWithBlocks != null) {
                        cloudRepository.backupNoteToCloud(noteWithBlocks.note, noteWithBlocks.blocks)
                    }
                }
            }
        }
        
        return Result.success(Unit)
    }
}

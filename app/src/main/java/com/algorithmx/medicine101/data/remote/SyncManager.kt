package com.algorithmx.medicine101.data.remote

import android.util.Log
import com.algorithmx.medicine101.data.ContentBlockEntity
import com.algorithmx.medicine101.data.NoteEntity
import com.algorithmx.medicine101.data.NoteRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncManager @Inject constructor(
    private val localRepository: NoteRepository,
    private val cloudRepository: CloudSyncRepository
) {
    private val TAG = "SyncManager"

    suspend fun syncEverything(): Result<Unit> {
        Log.d(TAG, "Sync started: Pulling from cloud...")
        val cloudResult = cloudRepository.pullNotesFromCloud()
        
        if (cloudResult.isFailure) {
            val error = cloudResult.exceptionOrNull() ?: Exception("Unknown error")
            Log.e(TAG, "Cloud pull failed: ${error.message}")
            return Result.failure(error)
        }

        val cloudItems = cloudResult.getOrNull() ?: return Result.success(Unit)
        Log.d(TAG, "Pulled ${cloudItems.size} items from cloud. Processing local updates...")

        // CRITICAL: Sort items so folders are processed BEFORE notes.
        // This prevents Foreign Key constraint violations in Room.
        val sortedItems = cloudItems.sortedByDescending { it.first.isFolder }

        for ((cloudNote, cloudBlocks) in sortedItems) {
            try {
                val localNote = localRepository.getNoteById(cloudNote.id)

                // Sync Logic:
                // 1. If local missing OR cloud is newer -> Update local
                if (localNote == null || cloudNote.updatedAt > localNote.updatedAt) {
                    Log.d(TAG, "Syncing to Local: '${cloudNote.title}' (Cloud is newer: ${cloudNote.updatedAt} > Local: ${localNote?.updatedAt ?: 0})")
                    
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
                        isDeleted = cloudNote.isDeleted
                    )

                    localRepository.insertNote(noteEntity)

                    val blockEntities = cloudBlocks.map { cloudBlock ->
                        ContentBlockEntity(
                            blockId = cloudBlock.blockId.toLongOrNull() ?: 0L,
                            noteId = cloudNote.id,
                            type = cloudBlock.type,
                            content = cloudBlock.content,
                            orderIndex = cloudBlock.orderIndex,
                            tabName = cloudBlock.tabName
                        )
                    }
                    
                    localRepository.syncBlocks(cloudNote.id, blockEntities)
                } 
                // 2. If local is newer -> Push to cloud
                else if (localNote.updatedAt > cloudNote.updatedAt) {
                    Log.d(TAG, "Syncing to Cloud: '${localNote.title}' (Local is newer)")
                    val noteWithBlocks = localRepository.getNoteWithBlocks(localNote.id)
                    if (noteWithBlocks != null) {
                        cloudRepository.backupNoteToCloud(noteWithBlocks.note, noteWithBlocks.blocks)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync note '${cloudNote.title}': ${e.message}")
                // Continue with other notes
            }
        }
        
        Log.d(TAG, "Sync process completed successfully.")
        return Result.success(Unit)
    }
}

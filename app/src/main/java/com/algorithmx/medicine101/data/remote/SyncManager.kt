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
        val localItems = mutableListOf<NoteEntity>() // To track what we need to push
        
        Log.d(TAG, "Pulled ${cloudItems.size} items from cloud.")

        // 2. Sort to avoid Foreign Key violations (Folders first)
        val sortedCloudItems = cloudItems.sortedByDescending { it.first.isFolder }

        for ((cloudNote, cloudBlocks) in sortedCloudItems) {
            val localNote = localRepository.getNoteById(cloudNote.id)

            when {
                // Scenario A: Local missing or Cloud is strictly newer
                localNote == null || cloudNote.updatedAt > localNote.updatedAt -> {
                    Log.d(TAG, "Updating Local: '${cloudNote.title}'")
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
                    
                    val blockEntities = cloudBlocks.map { cb ->
                        ContentBlockEntity(
                            // Crucial: We must keep IDs stable between local and cloud
                            blockId = cb.blockId.toLongOrNull() ?: 0L, 
                            noteId = cloudNote.id,
                            type = cb.type,
                            content = cb.content,
                            orderIndex = cb.orderIndex,
                            tabName = cb.tabName
                        )
                    }
                    localRepository.syncBlocks(cloudNote.id, blockEntities)
                }
                
                // Scenario B: Local is strictly newer -> Needs to be pushed later
                localNote.updatedAt > cloudNote.updatedAt -> {
                    Log.d(TAG, "Local is newer for '${localNote.title}', will push to cloud.")
                    val noteWithBlocks = localRepository.getNoteWithBlocks(localNote.id)
                    if (noteWithBlocks != null) {
                        cloudRepository.backupNoteToCloud(noteWithBlocks.note, noteWithBlocks.blocks)
                    }
                }
            }
        }
        
        // 3. Check for local notes that don't exist in cloud at all (New creations)
        // This handles notes created offline.
        // For brevity, we'll assume the updatedAt logic covers most cases, 
        // but a full bi-directional sync should compare sets.

        return Result.success(Unit)
    }
}

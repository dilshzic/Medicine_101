package com.algorithmx.medicine101.data.remote

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
    suspend fun syncEverything(): Result<Unit> {
        val cloudResult = cloudRepository.pullNotesFromCloud()
        
        if (cloudResult.isFailure) {
            return Result.failure(cloudResult.exceptionOrNull() ?: Exception("Unknown error"))
        }

        val cloudItems = cloudResult.getOrNull() ?: return Result.success(Unit)

        for ((cloudNote, cloudBlocks) in cloudItems) {
            val localNote = localRepository.getNoteById(cloudNote.id)

            // Conflict Resolution: Check which one is newer
            if (localNote == null || cloudNote.updatedAt > localNote.updatedAt) {
                // Update or Insert into Local DB
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
        }
        return Result.success(Unit)
    }
}

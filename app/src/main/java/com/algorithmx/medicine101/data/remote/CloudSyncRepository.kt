package com.algorithmx.medicine101.data.remote

import com.algorithmx.medicine101.data.ContentBlockEntity
import com.algorithmx.medicine101.data.NoteEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val notesCollection = firestore.collection("notes")

    suspend fun backupNoteToCloud(note: NoteEntity, blocks: List<ContentBlockEntity>): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))

        return try {
            val cloudNote = CloudNote(
                id = note.id,
                userId = uid,
                title = note.title,
                category = note.category,
                tags = note.tags,
                type = note.type,
                isFolder = note.isFolder,
                parentId = note.parentId,
                sortOrder = note.sortOrder,
                isSystemNote = note.isSystemNote,
                lastModified = note.lastModified,
                isPinned = note.isPinned,
                createdAt = note.createdAt,
                updatedAt = note.updatedAt,
                isDeleted = note.isDeleted
            )

            val batch = firestore.batch()
            val noteRef = notesCollection.document(note.id)
            batch.set(noteRef, cloudNote)

            val blocksRef = noteRef.collection("blocks")
            blocks.forEach { block ->
                val blockDocId = block.blockId.toString() 
                val cloudBlock = CloudContentBlock(
                    blockId = blockDocId,
                    type = block.type,
                    content = block.content,
                    orderIndex = block.orderIndex,
                    tabName = block.tabName
                )
                batch.set(blocksRef.document(blockDocId), cloudBlock)
            }

            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun pullNotesFromCloud(): Result<List<Pair<CloudNote, List<CloudContentBlock>>>> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("User not authenticated"))
        
        return try {
            val querySnapshot = notesCollection
                .whereEqualTo("userId", uid)
                .whereEqualTo("isDeleted", false)
                .get()
                .await()

            val results = mutableListOf<Pair<CloudNote, List<CloudContentBlock>>>()

            for (document in querySnapshot.documents) {
                val cloudNote = document.toObject(CloudNote::class.java)
                if (cloudNote != null) {
                    val blocksSnapshot = document.reference.collection("blocks").get().await()
                    val cloudBlocks = blocksSnapshot.toObjects(CloudContentBlock::class.java)
                    results.add(cloudNote to cloudBlocks)
                }
            }
            Result.success(results)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

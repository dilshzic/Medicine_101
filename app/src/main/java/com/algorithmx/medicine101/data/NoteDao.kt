package com.algorithmx.medicine101.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    // 1. Explorer: Get items in Root (parentId is null AND not deleted)
    @Query("SELECT * FROM notes WHERE parentId IS NULL AND isDeleted = 0 ORDER BY isFolder DESC, title ASC")
    fun getRootItems(): Flow<List<NoteEntity>>

    // 2. Explorer: Get items in Folder
    @Query("SELECT * FROM notes WHERE parentId = :parentId AND isDeleted = 0 ORDER BY isFolder DESC, title ASC")
    fun getItemsInFolder(parentId: String): Flow<List<NoteEntity>>

    // 3. Editor: Get specific note
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    // 4. Search

    
    // 5. Seeding Check
    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
    
    // 6. Find Folder by Name (For Seeding)
    @Query("SELECT * FROM notes WHERE title = :title AND isFolder = 1 LIMIT 1")
    suspend fun getFolderByName(title: String): NoteEntity?

    // CRUD
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    // Soft Delete Implementation
    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())


    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithBlocks(noteId: String): NoteWithBlocks?

    @Transaction
    suspend fun syncBlocks(noteId: String, newBlocks: List<ContentBlockEntity>) {
        // 1. Delete all existing blocks for this note
        deleteBlocksByNoteId(noteId)
        // 2. Insert the fresh, correctly ordered list
        insertBlocks(newBlocks)
    }

    @Query("DELETE FROM content_blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksByNoteId(noteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<ContentBlockEntity>)
}
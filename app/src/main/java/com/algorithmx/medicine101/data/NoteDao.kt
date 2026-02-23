package com.algorithmx.medicine101.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE parentId IS NULL AND isDeleted = 0 ORDER BY isFolder DESC, title ASC")
    fun getRootItems(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE parentId = :parentId AND isDeleted = 0 ORDER BY isFolder DESC, title ASC")
    fun getItemsInFolder(parentId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNoteById(id: String): NoteEntity?

    @Query("SELECT COUNT(*) FROM notes")
    suspend fun getNoteCount(): Int
    
    @Query("SELECT * FROM notes WHERE title = :title AND isFolder = 1 LIMIT 1")
    suspend fun getFolderByName(title: String): NoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: NoteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<NoteEntity>)

    @Update
    suspend fun update(note: NoteEntity)

    @Query("UPDATE notes SET isDeleted = 1, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDelete(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun delete(id: String)

    @Transaction
    @Query("SELECT * FROM notes WHERE id = :noteId")
    suspend fun getNoteWithBlocks(noteId: String): NoteWithBlocks?

    @Transaction
    suspend fun syncBlocks(noteId: String, newBlocks: List<ContentBlockEntity>) {
        deleteBlocksByNoteId(noteId)
        insertBlocks(newBlocks)
    }

    @Query("DELETE FROM content_blocks WHERE noteId = :noteId")
    suspend fun deleteBlocksByNoteId(noteId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlocks(blocks: List<ContentBlockEntity>)

    @Query("UPDATE notes SET parentId = :newParentId, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateParent(id: String, newParentId: String?, timestamp: Long = System.currentTimeMillis())

    @Query("SELECT * FROM notes WHERE isFolder = 1 AND isDeleted = 0 AND id != :excludeId")
    suspend fun getAllFoldersExcept(excludeId: String): List<NoteEntity>

    @Query("SELECT * FROM notes WHERE isFolder = 1 AND isDeleted = 0")
    suspend fun getAllFolders(): List<NoteEntity>

    // DASHBOARD QUERIES
    @Query("SELECT * FROM notes WHERE isPinned = 1 AND isDeleted = 0 ORDER BY updatedAt DESC")
    fun getPinnedNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE isDeleted = 0 ORDER BY lastModified DESC LIMIT 10")
    fun getRecentNotes(): Flow<List<NoteEntity>>

    @Query("UPDATE notes SET isPinned = :pinned, updatedAt = :timestamp WHERE id = :id")
    suspend fun updatePinStatus(id: String, pinned: Boolean, timestamp: Long = System.currentTimeMillis())

    // SEARCH
    @Query("SELECT * FROM notes WHERE (title LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%') AND isDeleted = 0")
    fun searchNotes(query: String): Flow<List<NoteEntity>>
}
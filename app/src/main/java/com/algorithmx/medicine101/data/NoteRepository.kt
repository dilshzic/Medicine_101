package com.algorithmx.medicine101.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    // --- READ OPERATIONS ---

    // Get Root Folders/Notes (Home Screen)
    fun getRootItems(): Flow<List<NoteEntity>> = noteDao.getRootItems()

    // Get Contents of a Folder
    fun getItemsInFolder(folderId: String): Flow<List<NoteEntity>> = noteDao.getItemsInFolder(folderId)

    // Get a specific note for the Editor
    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    // Search
    //fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    // --- WRITE OPERATIONS ---

    suspend fun insertNote(note: NoteEntity) = noteDao.insert(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)

    suspend fun softDeleteNote(id: String) = noteDao.softDelete(id)

    // --- SEEDING HELPER ---
    
    // Check if DB is empty to trigger first-run logic
    suspend fun isDatabaseEmpty(): Boolean {
        return noteDao.getNoteCount() == 0
    }

    suspend fun getNoteWithBlocks(noteId: String): NoteWithBlocks? {
        return noteDao.getNoteWithBlocks(noteId)

    }

    suspend fun syncBlocks(noteId: String, blockEntities: List<ContentBlockEntity>) {
        noteDao.syncBlocks(noteId, blockEntities)
    }
}
package com.algorithmx.medicine101.data

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getRootItems(): Flow<List<NoteEntity>> = noteDao.getRootItems()

    fun getItemsInFolder(folderId: String): Flow<List<NoteEntity>> = noteDao.getItemsInFolder(folderId)

    suspend fun getNoteById(id: String): NoteEntity? = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) = noteDao.insert(note)

    suspend fun updateNote(note: NoteEntity) = noteDao.update(note)

    suspend fun softDeleteNote(id: String) = noteDao.softDelete(id)

    suspend fun deleteNote(id: String) = noteDao.delete(id)

    suspend fun moveNote(id: String, newParentId: String?) = noteDao.updateParent(id, newParentId)

    suspend fun getAllFoldersExcept(excludeId: String) = noteDao.getAllFoldersExcept(excludeId)

    suspend fun getAllFolders() = noteDao.getAllFolders()

    // DASHBOARD
    fun getPinnedNotes(): Flow<List<NoteEntity>> = noteDao.getPinnedNotes()
    
    fun getRecentNotes(): Flow<List<NoteEntity>> = noteDao.getRecentNotes()

    suspend fun updatePinStatus(id: String, isPinned: Boolean) = noteDao.updatePinStatus(id, isPinned)

    // SEARCH
    fun searchNotes(query: String): Flow<List<NoteEntity>> = noteDao.searchNotes(query)

    // --- SEEDING HELPER ---
    suspend fun isDatabaseEmpty(): Boolean = noteDao.getNoteCount() == 0

    suspend fun getNoteWithBlocks(noteId: String): NoteWithBlocks? = noteDao.getNoteWithBlocks(noteId)

    suspend fun syncBlocks(noteId: String, blockEntities: List<ContentBlockEntity>) {
        noteDao.syncBlocks(noteId, blockEntities)
    }

    suspend fun insertBlocks(blocks: List<ContentBlockEntity>) = noteDao.insertBlocks(blocks)
}
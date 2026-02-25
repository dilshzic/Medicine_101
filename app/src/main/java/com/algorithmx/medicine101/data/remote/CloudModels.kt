package com.algorithmx.medicine101.data.remote

// Mirrors NoteEntity but adds userId for security rules
data class CloudNote(
    val id: String = "",
    val userId: String = "", 
    val title: String = "",
    val category: String = "",
    val tags: String = "",
    val type: String = "note",
    val isFolder: Boolean = false,
    val parentId: String? = null,
    val sortOrder: Int = 0,
    val isSystemNote: Boolean = false,
    val lastModified: Long = 0,
    val isPinned: Boolean = false,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
    val isDeleted: Boolean = false
)

// Mirrors ContentBlockEntity
data class CloudContentBlock(
    val blockId: String = "", // Changed to String for Firestore document IDs
    val type: String = "",
    val content: String = "",
    val orderIndex: Int = 0,
    val tabName: String = "General"
)
